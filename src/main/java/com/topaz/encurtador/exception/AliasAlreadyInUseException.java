package com.topaz.encurtador.exception;

public class AliasAlreadyInUseException extends RuntimeException {

    public AliasAlreadyInUseException(String alias) {
        super("O alias '" + alias + "' ja esta em uso.");
    }
}
