package com.neshan.urlshortener.repo;

import com.neshan.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends CrudRepository<ShortUrl,String> {
    Optional<List<ShortUrl>> findByUsername(String username);
    @Modifying
    public void deleteByLastVisitBefore(Date expiryDate);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default Optional<ShortUrl> findByIdAndIncrementVisit(String url){
        incrementVisitAndUpdateLastVisit(url);
        return findById(url);
    }

    @Modifying
    @Transactional
    @Query("UPDATE ShortUrl s set s.visitCount = s.visitCount + 1 , s.lastVisit= CURRENT_DATE WHERE s.shortUrl = :url")
    void incrementVisitAndUpdateLastVisit(String url);
}
