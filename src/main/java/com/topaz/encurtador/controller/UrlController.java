package com.topaz.encurtador.controller;

import com.topaz.encurtador.dto.request.CreateShortUrlRequest;
import com.topaz.encurtador.dto.response.CreateShortUrlResponse;
import com.topaz.encurtador.exception.InvalidUrlException;
import com.topaz.encurtador.model.ShortUrl;
import com.topaz.encurtador.service.UrlShortenerService;
import com.topaz.encurtador.web.RedirectServlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/urls")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UrlController {

    @Inject
    private UrlShortenerService service;

    @Context
    private HttpServletRequest httpRequest;

    @POST
    public Response create(CreateShortUrlRequest request) {
        if (request == null) {
            throw new InvalidUrlException("Corpo da requisicao ausente.");
        }

        ShortUrl created = service.create(request.getUrl(), request.getAlias());
        String shortUrl = buildShortUrl(created.getShortCode());

        CreateShortUrlResponse body = new CreateShortUrlResponse(
                created.getOriginalUrl(),
                created.getShortCode(),
                shortUrl);

        return Response.created(URI.create(shortUrl)).entity(body).build();
    }

    private String buildShortUrl(String code) {
        String base = httpRequest.getScheme() + "://"
                + httpRequest.getServerName() + ":" + httpRequest.getServerPort()
                + httpRequest.getContextPath();
        return base + RedirectServlet.PREFIX + code;
    }
}
