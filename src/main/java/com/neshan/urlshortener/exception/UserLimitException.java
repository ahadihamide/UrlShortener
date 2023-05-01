package com.neshan.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class UserLimitException extends ApiException{

    public UserLimitException(String message) {
        super(message,"TOO_MANY_REQUESTS");
    }
}
