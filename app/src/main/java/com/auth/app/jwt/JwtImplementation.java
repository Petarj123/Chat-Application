package com.auth.app.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

public interface JwtImplementation {

    String generateToken(HashMap<String, Object> extraClaims, UserDetails userDetails);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
    Date extractExpirationDate(String token);
    Claims extractAllClaims(String token);
    String extractEmail(String token);
    String extractRole(String token);
    String extractId(String token);
    Key getSignInKey();
}
