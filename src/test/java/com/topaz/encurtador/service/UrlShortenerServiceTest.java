package com.topaz.encurtador.service;

import com.topaz.encurtador.exception.AliasAlreadyInUseException;
import com.topaz.encurtador.exception.InvalidAliasException;
import com.topaz.encurtador.exception.InvalidUrlException;
import com.topaz.encurtador.exception.ShortUrlNotFoundException;
import com.topaz.encurtador.model.ShortUrl;
import com.topaz.encurtador.repository.UrlRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlShortenerServiceTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private ShortCodeGenerator codeGenerator;

    @InjectMocks
    private UrlShortenerService service;

    private void stubSaveEcho() {
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ------------------------------------------------------------------
    // create() - caminho feliz com codigo gerado
    // ------------------------------------------------------------------

    @Test
    public void create_semAlias_geraCodigoESalva() {
        when(codeGenerator.generate()).thenReturn("abc123");
        when(repository.existsByShortCode("abc123")).thenReturn(false);
        stubSaveEcho();

        ShortUrl result = service.create("https://example.com", null);

        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortCode());

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(repository).save(captor.capture());
        assertEquals("abc123", captor.getValue().getShortCode());
    }

    @Test
    public void create_normalizaUrlComEspacos() {
        when(codeGenerator.generate()).thenReturn("xyz789");
        when(repository.existsByShortCode("xyz789")).thenReturn(false);
        stubSaveEcho();

        ShortUrl result = service.create("   https://example.com   ", null);

        assertEquals("https://example.com", result.getOriginalUrl());
    }

    @Test
    public void create_quandoCodigoColide_tentaNovamente() {
        // primeiro codigo ja existe, segundo esta livre
        when(codeGenerator.generate()).thenReturn("dup000", "free01");
        when(repository.existsByShortCode("dup000")).thenReturn(true);
        when(repository.existsByShortCode("free01")).thenReturn(false);
        stubSaveEcho();

        ShortUrl result = service.create("https://example.com", null);

        assertEquals("free01", result.getShortCode());
        verify(codeGenerator, times(2)).generate();
    }

    @Test(expected = IllegalStateException.class)
    public void create_quandoNuncaAchaCodigoUnico_lancaIllegalState() {
        when(codeGenerator.generate()).thenReturn("dup000");
        when(repository.existsByShortCode("dup000")).thenReturn(true);

        service.create("https://example.com", null);
    }

    // ------------------------------------------------------------------
    // create() - com alias
    // ------------------------------------------------------------------

    @Test
    public void create_comAliasDisponivel_usaAlias() {
        when(repository.existsByShortCode("meulink")).thenReturn(false);
        stubSaveEcho();

        ShortUrl result = service.create("https://example.com", "meulink");

        assertEquals("meulink", result.getShortCode());
        verify(codeGenerator, never()).generate();
    }

    @Test(expected = AliasAlreadyInUseException.class)
    public void create_comAliasEmUso_lancaConflito() {
        when(repository.existsByShortCode("ocupado")).thenReturn(true);

        service.create("https://example.com", "ocupado");
    }

    @Test(expected = AliasAlreadyInUseException.class)
    public void create_comAliasReservado_lancaConflito() {
        service.create("https://example.com", "api");
    }

    @Test(expected = InvalidAliasException.class)
    public void create_comAliasMuitoCurto_lancaInvalido() {
        service.create("https://example.com", "ab");
    }

    @Test(expected = InvalidAliasException.class)
    public void create_comAliasComCaracteresInvalidos_lancaInvalido() {
        service.create("https://example.com", "com espaco");
    }

    // ------------------------------------------------------------------
    // create() - validacao de URL
    // ------------------------------------------------------------------

    @Test(expected = InvalidUrlException.class)
    public void create_comUrlNula_lancaInvalida() {
        service.create(null, null);
    }

    @Test(expected = InvalidUrlException.class)
    public void create_comUrlVazia_lancaInvalida() {
        service.create("   ", null);
    }

    @Test(expected = InvalidUrlException.class)
    public void create_comUrlSemProtocolo_lancaInvalida() {
        service.create("www.google.com", null);
    }

    @Test(expected = InvalidUrlException.class)
    public void create_comProtocoloNaoHttp_lancaInvalida() {
        service.create("ftp://arquivos.example.com", null);
    }

    // ------------------------------------------------------------------
    // resolve()
    // ------------------------------------------------------------------

    @Test
    public void resolve_quandoExiste_retornaEntidade() {
        ShortUrl stored = new ShortUrl("https://example.com", "abc123");
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(stored));

        ShortUrl result = service.resolve("abc123");

        assertSame(stored, result);
    }

    @Test(expected = ShortUrlNotFoundException.class)
    public void resolve_quandoNaoExiste_lancaNotFound() {
        when(repository.findByShortCode(anyString())).thenReturn(Optional.empty());

        service.resolve("inexistente");
    }
}
