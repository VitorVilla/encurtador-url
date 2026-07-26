package com.topaz.encurtador.exception;

import javax.ws.rs.core.Response;

/** Alias solicitado ja em uso (ou reservado) -> HTTP 409. */
public class AliasAlreadyInUseException extends BusinessException {

    public AliasAlreadyInUseException(String alias) {
        super(Response.Status.CONFLICT.getStatusCode(),
                "O alias '" + alias + "' ja esta em uso.");
    }
}
