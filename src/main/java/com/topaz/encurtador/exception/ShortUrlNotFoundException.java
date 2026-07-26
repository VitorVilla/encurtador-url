package com.topaz.encurtador.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String shortCode) {
        super("Nenhuma URL encontrada para o codigo '" + shortCode + "'.");
    }
}
