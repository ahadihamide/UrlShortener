package com.neshan.urlshortener.entity;

import java.sql.Date;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "short_url")
public class ShortUrl {
  @Id
  @Column(name = "short_url")
  private String shortUrl;

  private String username;

  @Column(name = "last_visit")
  private Date lastVisit;

  @Column(name = "long_url")
  private String longUrl;

  @Column(name = "visit_count")
  private Long visitCount;

  public ShortUrl(String user, String shortUrl, String longUrl, LocalDate creationDate, long visitCount) {
    this.shortUrl = shortUrl;
    this.username = user;
    this.lastVisit = java.sql.Date.valueOf(creationDate);
    this.longUrl = longUrl;
    this.visitCount = visitCount;
  }
}
