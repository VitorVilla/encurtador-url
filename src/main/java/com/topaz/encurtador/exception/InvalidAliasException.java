package com.topaz.encurtador.exception;

import javax.ws.rs.core.Response;

/** Alias com formato invalido (tamanho ou caracteres) -> HTTP 400. */
public class InvalidAliasException extends BusinessException {

    public InvalidAliasException(String message) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), message);
    }
}
