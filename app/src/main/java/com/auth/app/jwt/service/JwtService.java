package com.auth.app.jwt.service;

import com.auth.app.exceptions.InvalidUsernameException;
import com.auth.app.model.user.model.Role;
import com.auth.app.model.user.model.SecureUser;
import com.auth.app.model.user.model.User;
import com.auth.app.model.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final UserRepository userRepository;
    private final Long expiration;
    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") Long expiration, UserRepository userRepository) {
        this.expiration = expiration;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.userRepository = userRepository;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        SecureUser secureUser = (SecureUser) authentication.getPrincipal();
        String email = secureUser.getEmail();
        String id = secureUser.getId();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + expiration);
        return Jwts.builder()
                .setSubject(username)
                .claim("id", id)
                .claim("roles", roles)
                .claim("email", email)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }
    public String generateToken(String username) throws InvalidUsernameException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new InvalidUsernameException("User with username " + username + " does not exist."));

        List<String> roles = user.getRoles().stream().map(Role::toString).toList();
        String email = user.getEmail();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("email", email)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + (7 * 24 * 60 * 60 * 1000));  // 7 days in milliseconds
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // Even if the JWT has expired, we still want to extract the username
            return e.getClaims().getSubject();
        }
    }

    public String getEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }
    public String getId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("id", String.class);
    }
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public Date extractExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}
