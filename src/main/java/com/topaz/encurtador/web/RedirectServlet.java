package com.topaz.encurtador.web;

import com.topaz.encurtador.exception.ShortUrlNotFoundException;
import com.topaz.encurtador.model.ShortUrl;
import com.topaz.encurtador.service.UrlShortenerService;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Atende as URLs curtas e redireciona para a URL original.
 *
 * Fica FORA do JAX-RS (que esta preso em /api) para que a URL curta seja limpa:
 *   http://host/encurtador-url/u/abc123
 *
 * Como ExceptionMapper so vale dentro do JAX-RS, este servlet trata o
 * "codigo nao encontrado" por conta propria, devolvendo 404.
 */
@WebServlet(urlPatterns = RedirectServlet.PREFIX + "*")
public class RedirectServlet extends HttpServlet {

    /** Prefixo das URLs curtas. Reutilizado pelo UrlController ao montar o link. */
    public static final String PREFIX = "/u/";

    @Inject
    private UrlShortenerService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = extractCode(req.getPathInfo());

        if (code == null || code.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Codigo curto ausente.");
            return;
        }

        try {
            ShortUrl shortUrl = service.resolve(code);
            // 302 Found + Location: setamos manualmente para enviar a URL original
            // exatamente como esta, sem qualquer resolucao relativa.
            resp.setStatus(HttpServletResponse.SC_FOUND);
            resp.setHeader("Location", shortUrl.getOriginalUrl());
        } catch (ShortUrlNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Extrai o codigo do path. Para "/u/abc123" o container entrega
     * pathInfo = "/abc123"; pegamos o primeiro segmento.
     */
    private String extractCode(String pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int slash = path.indexOf('/');
        return slash >= 0 ? path.substring(0, slash) : path;
    }
}
