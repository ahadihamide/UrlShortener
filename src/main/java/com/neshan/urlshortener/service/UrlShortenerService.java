package com.neshan.urlshortener.service;

import com.neshan.urlshortener.exception.UserLimitException;
import reactor.core.publisher.Mono;

public interface UrlShortenerService {

    Mono<String> shortenUrl(String longUrl, String username) throws UserLimitException;

    Mono<String> getLongUrlAndIncrementVisit(String shortUrl);

    void delete(String shortUrl, String userName);

    Mono<Long> getVisit(String shortUrl);

}
