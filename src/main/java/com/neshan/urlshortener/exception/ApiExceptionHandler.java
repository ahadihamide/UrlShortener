package com.neshan.urlshortener.exception;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(InvalidTokenException.class)
  public Mono<ResponseEntity<String>> handleInvalidTokenException(InvalidTokenException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage()));
  }

  @ExceptionHandler(AuthorizationException.class)
  public Mono<ResponseEntity<String>> handleInvalidTokenException(AuthorizationException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage()));
  }

  @ExceptionHandler(UserLimitException.class)
  public Mono<ResponseEntity<String>> handleUserLimitException(UserLimitException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<String>> handleInvalidShortUrlException(IllegalArgumentException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
  }

  @ExceptionHandler(EmptyResultDataAccessException.class)
  public Mono<ResponseEntity<String>> handleInvalidShortUrlException(
      EmptyResultDataAccessException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body(ex.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public Mono<ResponseEntity<String>> handleAuthenticationException(AuthenticationException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage()));
  }
}
