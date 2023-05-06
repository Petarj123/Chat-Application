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

    /**
     This method performs the authentication and authorization logic for the application.
     The method checks the "Authorization" header of the HTTP request to see if it contains a valid JWT token.
     If the token is valid and not expired, the method sets the authentication context for the current user.
     If the token is expired, the method checks for a valid refresh token and generates a new JWT token if possible.
     If the token is invalid or no token is present, the method proceeds to the next filter in the filter chain.
     @param request the HTTP request to authenticate and authorize.
     @param response the HTTP response to return.
     @param filterChain the filter chain that contains the other filters to execute.
     @throws ServletException if there is an error with the servlet or the filter chain.
     @throws IOException if there is an error with the input or output streams of the HTTP request/response.
     */
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
