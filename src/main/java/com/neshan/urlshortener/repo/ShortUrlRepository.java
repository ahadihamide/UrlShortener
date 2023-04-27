package com.neshan.urlshortener.repo;

import com.neshan.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends CrudRepository<ShortUrl,String> {
    Optional<List<ShortUrl>> findByUsername(String username);
    @Modifying
    public void deleteByLastVisitBefore(Date expiryDate);
}
