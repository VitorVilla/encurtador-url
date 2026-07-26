package com.topaz.encurtador.exception;

import javax.ws.rs.core.Response;

/** URL original nula, vazia ou mal formada -> HTTP 400. */
public class InvalidUrlException extends BusinessException {

    public InvalidUrlException(String message) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), message);
    }
}
