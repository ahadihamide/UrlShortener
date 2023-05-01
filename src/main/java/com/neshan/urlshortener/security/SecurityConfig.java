package com.neshan.urlshortener.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
  private final JwtProvider jwtProvider;

  public SecurityConfig(JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Bean
  public SecurityWebFilterChain filterChain(
      ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager)
      throws Exception {
    return http.authorizeExchange()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/shorten")
        .authenticated()
        .pathMatchers(HttpMethod.DELETE, "/shorten")
        .authenticated()
        .and()
        .csrf()
        .disable()
        .httpBasic()
        .disable()
        .formLogin()
        .disable()
        .exceptionHandling()
        .authenticationEntryPoint(
            (swe, e) -> {
              log.info("[1] Authentication error: Unauthorized[401]: " + e.getMessage());

              return Mono.fromRunnable(
                  () -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
            })
        .accessDeniedHandler(
            (swe, e) -> {
              log.info("[2] Authentication error: Access Denied[401]: " + e.getMessage());

              return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
            })
        .and()
        .addFilterAt(
            bearerAuthenticationFilter(authenticationManager),
            SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange()
        .pathMatchers("/**")
        .permitAll()
        .and()
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  AuthenticationWebFilter bearerAuthenticationFilter(ReactiveAuthenticationManager authManager) {
    AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);
    bearerAuthenticationFilter.setAuthenticationConverter(
        new ServerHttpBearerAuthenticationConverter(this.jwtProvider));

    return bearerAuthenticationFilter;
  }
}
