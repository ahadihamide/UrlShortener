package com.neshan.urlshortener.security;

import com.neshan.urlshortener.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtProvider {
  @Value("${jwt.secret:mySecretKey}")
  private String jwtSecret;

  @Value("${jwt.expiration.in.ms:86400000}")
  private int jwtExpirationInMs;

  public String generateToken(String username) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public String getUserUsernameFromJWT(String token) {
    Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  public String validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return getUserUsernameFromJWT(authToken);
    } catch (SignatureException ex) {
      throw new UnauthorizedException("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      throw new UnauthorizedException("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      throw new UnauthorizedException("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      throw new UnauthorizedException("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      throw new UnauthorizedException("JWT claims string is empty");
    }
  }

  public Mono<String> check(String accessToken) {
    return Mono.just(validateToken(accessToken))
        .onErrorResume(e -> Mono.error(new UnauthorizedException(e.getMessage())));
  }
}
