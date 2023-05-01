package com.neshan.urlshortener.controller;

import com.neshan.urlshortener.exception.InvalidTokenException;
import com.neshan.urlshortener.exception.UserLimitException;
import com.neshan.urlshortener.security.InDBUserDetailsService;
import com.neshan.urlshortener.service.UrlShortenerService;
import com.neshan.urlshortener.utils.UrlUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class UrlShortenerController {
  private final UrlShortenerService urlShortenerService;
  private final InDBUserDetailsService userDetailsService;

  @Value("${base.url}")
  private String baseUrl;

  public UrlShortenerController(
      UrlShortenerService urlShortenerService, InDBUserDetailsService userDetailsService) {
    this.urlShortenerService = urlShortenerService;
    this.userDetailsService = userDetailsService;
  }

  @Operation(security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/shorten")
  public Mono<String> shortenUrl(@RequestParam String longUrl, Authentication authentication)
      throws InvalidTokenException, UserLimitException {
    return Mono.just(authentication.getName())
        .flatMap(userDetailsService::findByUsername)
        .filter(userDetails -> Objects.equals(authentication.getName(), userDetails.getUsername()))
        .flatMap(userDetails -> urlShortenerService.shortenUrl(longUrl, authentication.getName()))
        .map(baseUrl::concat);
  }
  // https://swagger.io/docs/specification/authentication/bearer-authentication/
  // https://stackoverflow.com/questions/33435286/swagger-ui-passing-authentication-token-to-api-call-in-header

  @GetMapping("/s/{shortUrl}")
  public Mono<ResponseEntity<Object>> redirectToLongUrl(
      @PathVariable String shortUrl, @RequestParam Map<String, String> queryParams) {
    return urlShortenerService
        .getLongUrlAndIncrementVisit(removeBaseUrl(shortUrl))
        .map(longUrl -> UrlUtils.addQueryParam(longUrl, queryParams))
        .map(
            longUrlWQuery ->
                ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, longUrlWQuery)
                    .build())
        .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("/visit")
  public Mono<Long> getVisits(@RequestParam String shortUrl) {
    return urlShortenerService.getVisit(removeBaseUrl(shortUrl));
  }

  @Operation(security = @SecurityRequirement(name = "bearerAuth"))
  @DeleteMapping("/shorten")
  public Mono<String> deleteShortenUrl(
      @RequestParam String shortUrl, Authentication authentication) {
    return userDetailsService
        .findByUsername(authentication.getName())
        .filter(userDetails -> Objects.equals(authentication.getName(), userDetails.getUsername()))
        .doOnNext(
            userDetails ->
                urlShortenerService.delete(removeBaseUrl(shortUrl), authentication.getName()))
        .map(userDetails -> "Successfully deleted!");
  }

  private String removeBaseUrl(String shortUrl) {
    if (StringUtils.isBlank(shortUrl)) throw new IllegalArgumentException("Invalid short url");

    return (shortUrl.contains(baseUrl)) ? shortUrl.substring(baseUrl.length()) : shortUrl;
  }
}
