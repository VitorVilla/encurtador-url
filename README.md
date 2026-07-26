# Encurtador de URL

Encurtador de URL feito para o desafio técnico da TOPAZ. A ideia é simples: você
manda uma URL longa, opcionalmente escolhe um apelido, e recebe um link curto que
redireciona pra original quando acessado.

Priorizei deixar o essencial redondo e bem explicado em vez de encher de features.
O tempo foi mais ou menos o sugerido no enunciado, então algumas coisas eu
deliberadamente deixei de fora — listo elas lá no final.

## Stack

- Java 8
- JAX-RS (REST) + Servlet (redirect)
- CDI + EJB
- JPA / Hibernate
- H2 (em memória)
- WildFly 10
- React no frontend (via CDN, sem build)
- JUnit 4 + Mockito nos testes
- Docker (opcional, pra rodar sem instalar nada)

## Como rodar

Você vai precisar de **Java 8**, **Maven** e um **WildFly 10**.

Gere o `.war`:

```bash
mvn clean package
```

O artefato sai em `target/encurtador-url.war`. Pra subir, é só jogar ele na pasta
de deployments do WildFly:

```bash
cp target/encurtador-url.war $WILDFLY_HOME/standalone/deployments/
$WILDFLY_HOME/bin/standalone.sh      # no Windows: standalone.bat
```

Não precisa configurar banco nenhum. A aplicação usa o `ExampleDS`, um datasource
H2 em memória que já vem pronto no WildFly 10 — comento o porquê mais abaixo.

Com o servidor no ar, abra:

```
http://localhost:8080/encurtador-url/
```

E os testes:

```bash
mvn test
```

### Rodando com Docker (a via mais fácil)

Se você tiver Docker, nem precisa de Java ou WildFly instalados. Um comando só:

```bash
docker compose up --build
```

Quando terminar, a aplicação está em `http://localhost:8080/encurtador-url/`.

O `Dockerfile` é multi-stage: um estágio com Maven compila o `.war`, e o estágio
final é o WildFly 10 oficial, que já recebe o artefato pronto e sobe com o `ExampleDS`
de sempre. A imagem final não carrega o Maven junto.

## A API

| Método | Rota | O que faz |
|--------|------|-----------|
| `POST` | `/api/urls` | cria uma URL curta |
| `GET`  | `/u/{codigo}` | redireciona (302) pra URL original |

Criando um link com código automático:

```bash
curl -i -X POST http://localhost:8080/encurtador-url/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
```

Resposta (`201 Created`):

```json
{
  "originalUrl": "https://www.google.com",
  "shortCode": "aB3xK9",
  "shortUrl": "http://localhost:8080/encurtador-url/u/aB3xK9"
}
```

Com apelido personalizado, é só mandar o campo `alias`:

```bash
curl -i -X POST http://localhost:8080/encurtador-url/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com","alias":"google"}'
```

Se o apelido já estiver em uso, volta um `409` com uma mensagem explicando. URL
inválida ou apelido mal formado voltam `400`. Todo erro sai num JSON padronizado:

```json
{ "status": 409, "message": "O alias 'google' ja esta em uso." }
```

## Estrutura

Organizei por camada, cada uma com uma responsabilidade só:

```
model/        entidade JPA (ShortUrl)
repository/   acesso a dados
service/      regras de negocio (o "motor")
controller/   endpoint REST de criacao
web/          servlet de redirecionamento
dto/          contratos de entrada e saida da API
exception/    excecoes de negocio + mappers pra HTTP
config/       configuracao do JAX-RS
```

## Decisões de design

### O motor de geração roda uma requisição por vez

Esse é o requisito que o enunciado destaca, então foi onde pensei mais. Em vez de
sair colocando `synchronized` na mão, usei a ferramenta que o próprio Java EE
oferece pra isso: um EJB `@Singleton` com `@Lock(LockType.WRITE)` no método de
criação.

```java
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class UrlShortenerService {

    @Lock(LockType.WRITE)               // uma requisicao por vez
    public ShortUrl create(...) { ... }

    @Lock(LockType.READ)                // leituras (redirect) em paralelo
    public ShortUrl resolve(...) { ... }
}
```

O container serializa o acesso ao `create` — se dois pedidos chegam juntos, um
espera o outro. De brinde, como é um método de EJB, ele já roda dentro de uma
transação gerenciada pelo container, então não preciso abrir/commitar transação na
mão.

Um efeito colateral que gostei: como a geração é serializada, o ciclo "gera código
→ verifica se já existe → salva" fica livre de condição de corrida por construção.
Dois pedidos nunca vão gerar o mesmo código ao mesmo tempo.

O redirect (`resolve`) usa `@Lock(READ)` porque não faz sentido serializar leitura.
O trade-off assumido é que, durante uma criação, os redirects esperam um instante.
Como a criação é rápida, o impacto é desprezível — mas em produção dá pra desacoplar
a leitura desse lock.

### Persistência com JTA e o ExampleDS

Optei por transações gerenciadas pelo container (JTA) em vez de `RESOURCE_LOCAL`.
É o jeito idiomático no WildFly: injeto o `EntityManager` com `@PersistenceContext`
e o servidor cuida de abrir e commitar a transação.

Pra conexão, usei o `ExampleDS` que já vem configurado no WildFly. A vantagem
prática é enorme pro "como rodar": ninguém precisa configurar banco, criar
datasource ou mexer no `standalone.xml`. Sobe o servidor e funciona. Num cenário
real, eu criaria um datasource dedicado no lugar de reaproveitar o de exemplo.

Detalhe relacionado: o Hibernate está com escopo `provided` no `pom.xml`, porque o
WildFly já traz a implementação dele. Empacotar uma versão diferente junto do `.war`
é pedido de conflito de classpath.

### Geração do código curto

Código de 6 caracteres em Base62 (`A-Z`, `a-z`, `0-9`), o que dá uns 56 bilhões de
combinações — mais que suficiente pro propósito. Uso `SecureRandom` pra deixar os
códigos imprevisíveis (dificulta alguém adivinhar o link dos outros).

Se por acaso o código sorteado já existir, tento de novo (até 5 vezes). Separei essa
geração numa classe própria (`ShortCodeGenerator`) porque ela é pura e ficou trivial
de testar isolada — quem cuida da unicidade é o service, que é quem conhece o banco.

### Redirect fora do `/api`

O JAX-RS está preso em `/api` (por causa do `@ApplicationPath`). Se eu fizesse o
redirect por ali, a URL curta ficaria tipo `/api/abc123`, que é feio. E mapear o
JAX-RS na raiz atrapalharia o frontend e os arquivos estáticos.

Então o redirect é um `Servlet` num prefixo próprio (`/u/`). Cada coisa no seu
lugar, sem catch-all frágil engolindo rota: API em `/api`, redirect em `/u`,
frontend na raiz.

### Tratamento de erros

As regras de negócio lançam exceções semânticas que herdam de uma base
`BusinessException`, a qual carrega o status HTTP correspondente. Um único
`BusinessExceptionMapper` traduz todas elas pra JSON — o RESTEasy resolve subindo
a hierarquia (`AliasAlreadyInUseException` → `BusinessException` → mapper). Assim o
contrato de erro fica consistente e num lugar só.

Duas pegadinhas do WildFly que só apareceram testando dentro do container (e que
reforçam o valor de testar no ambiente real, não só com mock):

- Exceção lançada de dentro de um EJB é embrulhada em `EJBException` por padrão,
  virando 500. Resolvi anotando a base com `@ApplicationException(rollback = true,
  inherited = true)`, que faz o container propagar a exceção intacta.
- Nessa versão do RESTEasy (3.0.x), registrar um `ExceptionMapper` por tipo se
  mostrou pouco confiável — um dos mappers simplesmente não era resolvido. Consolidar
  num mapper de base resolveu de vez e ainda deixou o código mais enxuto.

### Frontend em React sem build

O frontend é um `index.html` único com React carregado via CDN — o Babel compila o
JSX no navegador. Foi uma escolha consciente: o enunciado pede algo simples, e montar
um pipeline com Node/npm/Vite só pra uma telinha adicionaria bastante complexidade ao
"como rodar". Do jeito que está, o frontend viaja dentro do próprio `.war` e não
exige nenhuma ferramenta extra.

Fica claro que Babel no navegador não é coisa de produção — lá eu usaria um build de
verdade gerando estáticos. Pro escopo do desafio, achei o equilíbrio certo.

Sobre as cores: baseei a paleta na identidade da TOPAZ (tema claro, azul sobre fundo
claro) usando variáveis CSS. Não consegui o hex exato do guia de marca, então deixei
tudo parametrizado no `:root` — trocando uma variável, o visual inteiro se ajusta.

## O que eu faria com mais tempo

Foram coisas que ficaram de fora conscientemente pra respeitar o escopo:

- **Teste de integração e de concorrência.** Os testes atuais são unitários e cobrem
  bem a lógica do service. Eu adicionaria um teste do repositório com H2 real e,
  principalmente, um teste que dispara vários `create` em paralelo pra *provar* que a
  serialização funciona — hoje ela está garantida pelo container, mas não coberta por
  teste.
- **Datasource dedicado** em vez do `ExampleDS`, e migração de schema com Flyway no
  lugar do `hbm2ddl=update` (que serve pra desenvolvimento, mas não pra produção).
- **Frontend com build real** (Vite), caso a tela crescesse.
- **Códigos derivados do ID** (Base62 do id sequencial), que eliminaria até o retry de
  colisão — deixei o random por ser mais simples de ler e já ser seguro nesse contexto.
- **Features de produto**: expiração de links, contador de acessos, listagem paginada e
  autenticação. Nenhuma essencial pro desafio.
- **Bean Validation** (`@Valid`) nos DTOs, movendo parte da validação pra anotações.
