package com.neshan.urlshortener.exception;

import lombok.Getter;

/**
 * ApiException class
 *
 * @author Erik Amaru Ortiz
 */
public class ApiException extends RuntimeException {
    @Getter
    protected String errorCode;

    public ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
