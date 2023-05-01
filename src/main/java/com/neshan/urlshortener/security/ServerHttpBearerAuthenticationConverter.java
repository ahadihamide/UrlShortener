package com.neshan.urlshortener.security;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ServerHttpBearerAuthenticationConverter class
 * This is a Converter that validates TOKEN against requests coming from AuthenticationFilter ServerWebExchange.
 *
 * @author Erik Amaru Ortiz
 * <a href="https://github.com/eriknyk/webflux-jwt-security-demo">...</a>
 */
public class ServerHttpBearerAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
    private static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String, Mono<String>> isolateBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER.length()));
    private final JwtProvider jwtVerifier;

    public ServerHttpBearerAuthenticationConverter(JwtProvider jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange)
                .flatMap(ServerHttpBearerAuthenticationConverter::extract)
                .filter(matchBearerLength)
                .flatMap(isolateBearerValue)
                .flatMap(jwtVerifier::check)
                .flatMap(ServerHttpBearerAuthenticationConverter::create);
    }

    public static Mono<String> extract(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION));
    }
    public static Mono<Authentication> create(String verificationResult) {

        if (StringUtils.isBlank(verificationResult))
            return Mono.empty(); // invalid value for any of jwt auth parts

        return Mono.justOrEmpty(new UsernamePasswordAuthenticationToken(new UserPrincipal(verificationResult), null, List.of()));
    }

}
