package com.auth.app.jwt;

import com.auth.app.model.Admin;
import com.auth.app.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
public class JwtService implements JwtImplementation{

    @Value("${secret.key}")
    private final String SECRET_KEY;
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    
    @Override
    public String generateToken(HashMap<String, Object> extraClaims, UserDetails userDetails) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + 20000);
        if (userDetails instanceof User) {
            extraClaims.put("role", ((User) userDetails).getRole().name());
            extraClaims.put("id", ((User) userDetails).getUserId());
            return Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(expirationDate)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        }
        extraClaims.put("role", ((Admin) userDetails).getRole().name());
        extraClaims.put("id", ((Admin) userDetails).getId());
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    
    public String generateRefreshToken(UserDetails userDetails){
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    
    public String generateRefreshToken(HashMap<String, Object> extraClaims, UserDetails userDetails){
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + 604800000); // 7 days in milliseconds
        if (userDetails instanceof User) {
            extraClaims.put("role", ((User) userDetails).getRole().name());
            extraClaims.put("id", ((User) userDetails).getUserId());
            extraClaims.put("sub",((User) userDetails).getEmail());
            return Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(expirationDate)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        }
        extraClaims.put("role", ((Admin) userDetails).getRole().name());
        extraClaims.put("id", ((Admin) userDetails).getId());
        extraClaims.put("sub",((Admin) userDetails).getEmail());
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    
    public String refreshJWTToken(String refreshToken) {
        Claims claims = extractAllClaims(refreshToken);
        String username = claims.getSubject();
        String id = claims.get("id", String.class);
        String role = claims.get("role", String.class);
        Date now = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(20000 + now.getTime());
        HashMap<String, Object> extraClaims = new HashMap<String, Object>();
        extraClaims.put("id", id);
        extraClaims.put("role", role);
        extraClaims.put("sub", username);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    
    @Override
    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    
    @Override
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    
    public Claims extractAllClaimsSafe(String token) {
        try {
            return extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("sub", String.class));
    }

    
    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    
    @Override
    public String extractId(String token) {
        return extractClaim(token, claims -> claims.get("id", String.class));
    }

    
    @Override
    public Key getSignInKey() {
        byte[] signature = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(signature);
    }

    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaimsSafe(token);
        return claimsResolver.apply(claims);
    }
}
