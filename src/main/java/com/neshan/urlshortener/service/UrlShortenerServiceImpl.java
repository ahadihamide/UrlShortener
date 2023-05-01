package com.neshan.urlshortener.service;

import com.neshan.urlshortener.entity.ShortUrl;
import com.neshan.urlshortener.exception.UnauthorizedException;
import com.neshan.urlshortener.exception.UserLimitException;
import com.neshan.urlshortener.repo.ShortUrlRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@EnableScheduling
public class UrlShortenerServiceImpl implements UrlShortenerService {

  private final ShortUrlRepository shortUrlRepository;
  @Autowired private RedissonClient redissonClient;

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
    RLock lock = redissonClient.getLock(username);
    if (lock.tryLock()) {
      List<ShortUrl> userUrls = shortUrlRepository.findByUsername(username).orElse(List.of());
      try {
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
      } finally {
        lock.unlock();
      }
    }
    throw new UserLimitException("Too many request, try later.");
  }

  @Transactional
  @Override
  public Mono<String> getLongUrlAndIncrementVisit(String shortUrl) {
    return Mono.just(shortUrl)
        .map(url -> shortUrlRepository.findByIdAndIncrementVisit(url).orElseThrow())
        .map(ShortUrl::getLongUrl)
        .doOnError(t -> log.error(t.getMessage()));
  }

  @Override
  public void delete(String url, String username) {
    ShortUrl shortUrl =
        shortUrlRepository
            .findById(url)
            .orElseThrow(() -> new IllegalArgumentException("ShortUrl not found!"));
    if (!username.equals(shortUrl.getUsername()))
      throw new UnauthorizedException("ShortUrl Does not belong to this user!");
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

  @Scheduled(cron = "0 0 0 * * *") // run at midnight every day
  @Transactional
  public void deleteOldUrls() {
    LocalDate cutoffDate = LocalDate.now().minusYears(1);
    log.info("deleting urls older than {}", cutoffDate);
    RLock lock = redissonClient.getLock(cutoffDate.toString());
    if (lock.tryLock())
      try {
        shortUrlRepository.deleteByLastVisitBefore(java.sql.Date.valueOf((cutoffDate)));
      } finally {
        lock.unlock();
      }
  }
}
