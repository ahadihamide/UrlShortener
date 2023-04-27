package com.neshan.urlshortener.utils;

import com.neshan.urlshortener.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtUtil {

  private static final String secretKey = "mySecretKey";

  public static String generateToken(Authentication authentication) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + 3600000);

    return Jwts.builder()
        .setSubject(authentication.getName())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact();
  }

  public static String getUsernameFromToken(String token) throws InvalidTokenException {
    try {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    } catch (MalformedJwtException mex) {
      throw new InvalidTokenException("Invalid Token");
    }
  }

  public static boolean isValidateToken(String token, UserDetails userDetails) {
    try {
      return getUsernameFromToken(token).equals(userDetails.getUsername())
          && !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean isTokenExpired(String token) {
    Date expiryDate =
        Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
    return expiryDate.before(new Date());
  }
}
