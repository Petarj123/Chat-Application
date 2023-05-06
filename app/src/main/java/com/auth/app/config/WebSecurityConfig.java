package com.auth.app.config;

import com.auth.app.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The type Web security config.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationManager authenticationManager;

    /**
     * This method configures a security filter chain for the application using the HttpSecurity object.
     * The method disables CSRF protection, and allows unauthenticated access to requests that match "/api/auth/**".
     * All other requests require authentication.
     * The session creation policy is set to "STATELESS", which means that the application will not create or use sessions to store user data.
     * The authentication manager is set to the one defined in the application context.
     * The JWT authentication filter is added to the filter chain before the UsernamePasswordAuthenticationFilter, which is responsible for authenticating requests based on user credentials.
     * This filter chain is used to secure the application and enforce authentication for protected resources.
     *
     * @param http the HttpSecurity object used to configure the security filter chain.
     * @return a SecurityFilterChain that is used to secure the application.
     * @throws Exception if there is an error configuring the HttpSecurity object.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/auth/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationManager(authenticationManager)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
