package com.neshan.urlshortener.security;

import com.neshan.urlshortener.exception.UnauthorizedException;
import com.neshan.urlshortener.model.AuthenticationResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
  private final ReactiveUserDetailsService userService;
  private final PasswordEncoder passwordEncoder;

  private final JwtProvider jwtProvider;

  public AuthenticationManager(
      ReactiveUserDetailsService userService,
      PasswordEncoder passwordEncoder,
      JwtProvider jwtProvider) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtProvider = jwtProvider;
  }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    var principal = (UserPrincipal) authentication.getPrincipal();

    // TODO add more user validation logic here.
    return userService
        .findByUsername(principal.getName())
        .filter(UserDetails::isEnabled)
        .switchIfEmpty(Mono.error(new UnauthorizedException("User account is disabled.")))
        .map(user -> authentication);
  }

  public Mono<AuthenticationResponse> authenticateByPassword(Authentication authentication) {
    String username = authentication.getName();
    String presentedPassword = (String) authentication.getCredentials();
    return this.userService
        .findByUsername(username)
        .filter(
            (userDetails) ->
                this.passwordEncoder.matches(presentedPassword, userDetails.getPassword()))
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid Credentials"))))
        .map(this::createAuthenticationToken);
  }

  private AuthenticationResponse createAuthenticationToken(UserDetails userDetails) {
    return new AuthenticationResponse(jwtProvider.generateToken(userDetails.getUsername()));
  }
}
