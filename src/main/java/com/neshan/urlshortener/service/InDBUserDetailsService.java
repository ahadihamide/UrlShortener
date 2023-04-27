package com.neshan.urlshortener.service;

import com.neshan.urlshortener.entity.User;
import com.neshan.urlshortener.repo.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class InDBUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public InDBUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return Mono.just(new org.springframework.security.core.userdetails.User(
                user.get().getUsername(),
                user.get().getPassword(),
                new ArrayList<>()
        ));
    }

}

