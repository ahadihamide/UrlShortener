package com.neshan.urlshortener.exception;

public class UserLimitException extends RuntimeException{

    public UserLimitException(String message) {
        super(message);
    }
}
