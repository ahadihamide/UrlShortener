package com.neshan.urlshortener.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ShortenRequest {
    private String longUrl;
    private String authToken;
}
