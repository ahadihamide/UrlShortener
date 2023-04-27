package com.neshan.urlshortener.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthenticationRequest {
    private Object password;
    private Object username;
}
