package com.topaz.encurtador.exception;

import javax.ws.rs.core.Response;

/** Nenhum registro corresponde ao codigo curto informado -> HTTP 404. */
public class ShortUrlNotFoundException extends BusinessException {

    public ShortUrlNotFoundException(String shortCode) {
        super(Response.Status.NOT_FOUND.getStatusCode(),
                "Nenhuma URL encontrada para o codigo '" + shortCode + "'.");
    }
}
