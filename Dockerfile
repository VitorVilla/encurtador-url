# ---------------------------------------------------------------------------
# Estagio 1: build
# Compila o projeto e gera o .war usando Maven + JDK 8.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-8 AS build

WORKDIR /app

# Copiamos so o pom primeiro e baixamos as dependencias numa camada separada.
# Assim, mudar codigo-fonte nao invalida o cache de dependencias (build mais rapido).
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Estagio 2: runtime
# Imagem oficial do WildFly 10 (Java 8, ja traz o datasource ExampleDS).
# So copiamos o .war pronto para a pasta de deployments.
# ---------------------------------------------------------------------------
FROM jboss/wildfly:10.1.0.Final

COPY --from=build /app/target/encurtador-url.war \
     /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080

# O CMD ja vem da imagem base: standalone.sh -b 0.0.0.0 (aceita conexoes externas).
