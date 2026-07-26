package com.topaz.encurtador.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateShortUrlRequest {

    private String url;
    private String alias;
}