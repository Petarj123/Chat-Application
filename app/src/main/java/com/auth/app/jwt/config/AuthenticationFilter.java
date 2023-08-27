package com.auth.app.jwt.config;



import com.auth.app.exceptions.ExpiredRefreshTokenException;
import com.auth.app.exceptions.InvalidUsernameException;
import com.auth.app.jwt.service.JwtService;
import com.auth.app.model.user.model.User;
import com.auth.app.model.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        String token = getTokenFromHeader(request);

        if (StringUtils.hasText(token)) {
            if (jwtService.validateToken(token)) {
                String username = jwtService.getUsername(token);
                setAuthentication(username, request);
            } else {
                // JWT token has expired
                String username = null;
                try {
                    username = jwtService.getUsername(token);
                } catch (ExpiredJwtException e) {
                    // Handle the expired token gracefully without throwing an exception.
                }

                if (username != null) {
                    User user = userRepository.findByUsername(username).orElseThrow();

                    String refreshToken = user.getRefreshToken();
                    if (refreshToken != null) {
                        if (!jwtService.validateRefreshToken(refreshToken)) {
                            throw new ExpiredRefreshTokenException("Refresh token has expired. Please log in again.");
                        } else {
                            Date refreshTokenExpiry = jwtService.extractExpirationDate(refreshToken);
                            if (refreshTokenExpiry.before(new Date())) {
                                // The refresh token itself has expired
                                throw new ExpiredRefreshTokenException("Refresh token has expired. Please log in again.");
                            } else {
                                // The refresh token is still valid, renew the JWT
                                setAuthentication(username, request);

                                // Generate a new JWT token
                                String newToken = null;
                                try {
                                    newToken = jwtService.generateToken(username);
                                } catch (InvalidUsernameException e) {
                                    throw new RuntimeException(e);
                                }

                                // Update the JWT token in the response headers
                                response.addHeader("Authorization", "Bearer " + newToken);
                            }
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }


    private void setAuthentication(String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getTokenFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
