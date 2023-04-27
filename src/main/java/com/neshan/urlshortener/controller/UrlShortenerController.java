package com.neshan.urlshortener.controller;

import com.neshan.urlshortener.exception.InvalidTokenException;
import com.neshan.urlshortener.exception.UserLimitException;
import com.neshan.urlshortener.model.DeleteRequest;
import com.neshan.urlshortener.model.ShortenRequest;
import com.neshan.urlshortener.service.InDBUserDetailsService;
import com.neshan.urlshortener.service.UrlShortenerService;
import com.neshan.urlshortener.utils.JwtUtil;
import com.neshan.urlshortener.utils.UrlUtils;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @PostMapping("/shorten")
  public Mono<String> shortenUrl(@RequestBody ShortenRequest req)
      throws InvalidTokenException, UserLimitException {
    String username = JwtUtil.getUsernameFromToken(req.getAuthToken());
    return Mono.just(username)
        .flatMap(userDetailsService::findByUsername)
        .filter(userDetails -> JwtUtil.isValidateToken(req.getAuthToken(), userDetails))
        .flatMap(userDetails -> urlShortenerService.shortenUrl(req.getLongUrl(), username))
        .map(baseUrl::concat);
  }

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

  @DeleteMapping("/shorten")
  public Mono<String> deleteShortenUrl(@RequestBody DeleteRequest req) {
    String username = JwtUtil.getUsernameFromToken(req.getAuthToken());
    return userDetailsService
        .findByUsername(username)
        .filter(userDetails -> JwtUtil.isValidateToken(req.getAuthToken(), userDetails))
        .doOnNext(
            userDetails -> urlShortenerService.delete(removeBaseUrl(req.getShortUrl()), username))
        .map(userDetails -> "Successfully deleted!");
  }

  private String removeBaseUrl(String shortUrl) {
    if (StringUtils.isBlank(shortUrl)) throw new IllegalArgumentException("Invalid short url");

    return (shortUrl.contains(baseUrl)) ? shortUrl.substring(baseUrl.length()) : shortUrl;
  }
}
