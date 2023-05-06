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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

/**
 * The JWTService class provides utility methods for generating and validating JWT tokens.
 */
@Service
@RequiredArgsConstructor
public class JwtService implements JwtImplementation{

    private final String SECRET_KEY;

    /**
     * Generates a JWT token with the given user details.
     *
     * @param userDetails the user details to generate the token for
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     Generates a JWT token with the given extra claims and user details.
     @param extraClaims the extra claims to add to the token
     @param userDetails the user details to generate the token for
     @return the generated JWT token
     */
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

    /**
     * Generates a JWT refresh token with the given user details.
     *
     * @param userDetails the user details to generate the refresh token for
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails){
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT refresh token with the given extra claims and user details.
     *
     * @param extraClaims the extra claims to add to the refresh token
     * @param userDetails the user details to generate the refresh token for
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(HashMap<String, Object> extraClaims, UserDetails userDetails){
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + 604800000); // 7 days in milliseconds
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

    /**
     * Refreshes a JWT token with the given refresh token.
     *
     * @param refreshToken the refresh token to use for refreshing the token
     * @return the refreshed JWT token
     */
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
        return Jwts.builder()
                .setSubject(username)
                .setClaims(extraClaims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     Checks if the given token is valid for the given user details.
     @param token the JWT token to validate
     @param userDetails the user details to validate the token against
     @return true if the token is valid, false otherwise
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     Checks if the given token is expired.
     @param token the JWT token to check
     @return true if the token is expired, false otherwise
     */
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    /**
     Extracts the expiration date from the given token.
     @param token the JWT token to extract the expiration date from
     @return the expiration date of the token
     */
    @Override
    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     Extracts all claims from the given token.
     @param token the JWT token to extract the claims from
     @return the claims of the token
     */
    @Override
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts all claims from the given token, ignoring any expired token exception.
     *
     * @param token the JWT token to extract the claims from
     * @return the claims of the token
     */
    public Claims extractAllClaimsSafe(String token) {
        try {
            return extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Extracts the email from the given JWT token.
     * @param token the JWT token
     * @return the email extracted from the token
     */
    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the role from the given JWT token.
     * @param token the JWT token
     * @return the role extracted from the token
     */
    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extracts the ID from the given JWT token.
     * @param token the JWT token
     * @return the ID extracted from the token
     */
    @Override
    public String extractId(String token) {
        return extractClaim(token, claims -> claims.get("id", String.class));
    }

    /**
     * Returns the key used for signing the JWT tokens.
     * @return the signing key
     */
    @Override
    public Key getSignInKey() {
        byte[] signature = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(signature);
    }

    /**
     * Extract claim t.
     *
     * @param <T>            the type parameter
     * @param token          the token
     * @param claimsResolver the claims resolver
     * @return the t
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaimsSafe(token);
        return claimsResolver.apply(claims);
    }
}
