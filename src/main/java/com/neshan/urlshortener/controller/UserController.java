package com.neshan.urlshortener.controller;

import com.neshan.urlshortener.entity.User;
import com.neshan.urlshortener.model.AuthenticationRequest;
import com.neshan.urlshortener.model.AuthenticationResponse;
import com.neshan.urlshortener.repo.UserRepository;
import com.neshan.urlshortener.security.AuthenticationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class UserController {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;

  public UserController(
      UserRepository userRepository, AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/signUp")
  public ResponseEntity<?> registerUser(@RequestBody AuthenticationRequest req) {
    if (userRepository.findByUsername(req.getUsername()).isPresent()) {
      return ResponseEntity.badRequest().body("Username already taken");
    }
    userRepository.save(new User(req.getUsername(),req.getPassword()));
    return ResponseEntity.ok("User registered successfully");
  }

  @PostMapping("/signIn")
  public Mono<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
    return authenticationManager.authenticateByPassword(token);
  }
}
