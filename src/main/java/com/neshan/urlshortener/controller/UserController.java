package com.neshan.urlshortener.controller;

import com.neshan.urlshortener.entity.User;
import com.neshan.urlshortener.model.AuthenticationRequest;
import com.neshan.urlshortener.model.AuthenticationResponse;
import com.neshan.urlshortener.repo.UserRepository;
import com.neshan.urlshortener.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class UserController {

  private final UserRepository userRepository;
  private final ReactiveAuthenticationManager authenticationManager;

  public UserController(
      UserRepository userRepository, ReactiveAuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody User user) {
    if (userRepository.findByUsername(user.getUsername()).isPresent()) {
      return ResponseEntity.badRequest().body("Username already taken");
    }
    userRepository.save(user);
    return ResponseEntity.ok("User registered successfully");
  }

  @PostMapping("/login")
  public Mono<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
    return authenticationManager
        .authenticate(token)
        .map(
            authentication -> {
              String jwt = JwtUtil.generateToken(authentication);
              return new AuthenticationResponse(jwt);
            });
  }
}
