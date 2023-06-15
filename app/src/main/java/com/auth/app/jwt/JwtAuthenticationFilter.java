package com.auth.app.jwt;

import com.auth.app.config.CustomUserDetailsService;
import com.auth.app.exceptions.InvalidTokenException;
import com.auth.app.exceptions.RefreshTokenExpiredException;
import com.auth.app.model.Admin;
import com.auth.app.model.User;
import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    
    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        final String header = request.getHeader("Authorization");
        final String jwt;
        final String username;
        final String role;

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = header.substring(7);
        username = jwtService.extractEmail(jwt);
        role = jwtService.extractRole(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsernameAndRole(username, role);
            if (jwtService.isTokenValid(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                if (jwtService.isTokenExpired(jwt)) {
                    String refreshToken = getRefreshToken(username);
                    if (!jwtService.isTokenExpired(refreshToken)){
                        String newJwt = jwtService.refreshJWTToken(refreshToken);
                        response.setHeader("Authorization", "Bearer " + newJwt);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        throw new RefreshTokenExpiredException("Refresh token has expired");
                    }
                }
                // If the token is not expired and not valid, it means the token is invalid.
                else {
                    throw new InvalidTokenException("Invalid JWT token");
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getRefreshToken(String username) throws IllegalAccessException {
        Optional<User> existingUser = userRepository.findByEmail(username);
        Optional<Admin> existingAdmin = adminRepository.findByEmail(username);

        if (existingUser.isPresent()) {
            User user = existingUser.orElseThrow();
            return user.getRefreshToken();
        } else if (existingAdmin.isPresent()) {
            Admin admin = existingAdmin.orElseThrow();
            return admin.getRefreshToken();
        } else {
            throw new IllegalAccessException("Error getting refresh token");
        }
    }

}
