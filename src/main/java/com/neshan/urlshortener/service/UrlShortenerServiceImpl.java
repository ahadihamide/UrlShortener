package com.neshan.urlshortener.service;

import com.neshan.urlshortener.entity.ShortUrl;
import com.neshan.urlshortener.exception.AuthorizationException;
import com.neshan.urlshortener.exception.UserLimitException;
import com.neshan.urlshortener.repo.ShortUrlRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

  private final ShortUrlRepository shortUrlRepository;

  public UrlShortenerServiceImpl(ShortUrlRepository shortUrlRepository) {
    this.shortUrlRepository = shortUrlRepository;
  }

  private static String hashUrl(String url) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] bytes = md.digest(url.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : bytes) {
        sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available");
    }
  }

  @Override
  public Mono<String> shortenUrl(String longUrl, String username) throws UserLimitException {
    List<ShortUrl> userUrls = shortUrlRepository.findByUsername(username).orElse(List.of());
    Optional<ShortUrl> existShortUrl =
        userUrls.stream().filter(shortUrl -> shortUrl.getLongUrl().equals(longUrl)).findAny();
    if (existShortUrl.isPresent()) return Mono.just(existShortUrl.get().getShortUrl());

    if (userUrls.size() >= 10) {
      throw new UserLimitException("User has already registered 10 shortened URLs");
    }
    return generateShortUrl(longUrl, username)
        .doOnNext(
            shortUrl ->
                shortUrlRepository.save(
                    new ShortUrl(username, shortUrl, longUrl, LocalDate.now(), 0L)));
  }

  @Override
  public Mono<String> getLongUrlAndIncrementVisit(String shortUrl) {
    return Mono.just(shortUrl)
        .map(url -> shortUrlRepository.findById(url).orElseThrow())
        .doOnNext(
            shortUrlEntity -> {
              shortUrlEntity.setVisitCount(shortUrlEntity.getVisitCount() + 1);
              shortUrlRepository.save(shortUrlEntity);
            })
        .map(ShortUrl::getLongUrl);
  }

  @Override
  public void delete(String url, String username) {
    ShortUrl shortUrl =
        shortUrlRepository
            .findById(url)
            .orElseThrow(() -> new IllegalArgumentException("ShortUrl not found!"));
    if (!username.equals(shortUrl.getUsername()))
      throw new AuthorizationException("ShortUrl Does not belong to this user!");
    shortUrlRepository.deleteById(url);
  }

  @Override
  public Mono<Long> getVisit(String url) {
    ShortUrl shortUrl =
        shortUrlRepository
            .findById(url)
            .orElseThrow(() -> new IllegalArgumentException("ShortUrl not found!"));
    return Mono.just(shortUrl.getVisitCount());
  }

  private Mono<String> generateShortUrl(String longUrl, String username) {
    return Mono.just(hashUrl(username + longUrl).substring(0, 7));
  }
}
