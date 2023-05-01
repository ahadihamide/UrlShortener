package com.neshan.urlshortener.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;

@Entity
@Getter @Setter @NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "user")
@Table(name = "user", schema = "public")
public class User {
    @Id
    private String username;
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    public void setPassword(String password) {
        this.password = new BCryptPasswordEncoder().encode(password);
    }

    public User(String username, String password) {
        setUsername(username);
        setPassword(password);
    }
}


