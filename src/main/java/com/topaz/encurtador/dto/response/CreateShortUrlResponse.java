package com.topaz.encurtador.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class CreateShortUrlResponse {

    private String originalUrl;
    private String shortCode;
    private String shortUrl;
}