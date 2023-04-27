package com.neshan.urlshortener.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteRequest {
    private String shortUrl;
    private String authToken;
}
