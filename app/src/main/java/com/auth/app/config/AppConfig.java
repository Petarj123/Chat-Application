package com.auth.app.config;

import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * The type App config.
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    @Value("${secret.key}")
    private String secretKey;

    /**
     * Returns an instance of BCryptPasswordEncoder that can be used to hash passwords for secure storage in a database.
     * This bean is used in authentication processes to check whether a provided password matches the hashed password stored in the database.
     *
     * @return an instance of BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Returns a lambda function that looks up a user by email in the userRepository and throws a UsernameNotFoundException if the user is not found.
     * This lambda function is used in authentication processes to retrieve the user's details for authentication.
     *
     * @return a lambda function that retrieves a user's details by email.
     */
    @Bean
    public UserDetailsService userDetailsService(){
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
    }

    /**
     * Returns a lambda function that looks up an admin by email in the adminRepository and throws a UsernameNotFoundException if the manager is not found.
     * This lambda function is used in authentication processes to retrieve the manager's details for authentication.
     *
     * @return a lambda function that retrieves an admin's details by email.
     */
    @Bean
    public UserDetailsService adminDetailsService(){
        return email -> adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin does not exist"));
    }

    /**
     * Returns an instance of DaoAuthenticationProvider that is configured to use the userDetailsService and passwordEncoder beans.
     * This provider is used to authenticate users during the authentication process.
     *
     * @return an instance of DaoAuthenticationProvider configured to use the userDetailsService and passwordEncoder beans.
     */
    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * Returns an instance of DaoAuthenticationProvider that is configured to use the adminDetailsService and passwordEncoder beans.
     * This provider is used to authenticate managers during the authentication process.
     *
     * @return an instance of DaoAuthenticationProvider configured to use the adminDetailsService and passwordEncoder beans.
     */
    @Bean
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(adminDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * Returns a ProviderManager that uses the userAuthenticationProvider and adminAuthenticationProvider beans to authenticate users and managers.
     * This bean is used to authenticate users and managers during the authentication process.
     *
     * @param configuration               the authentication configuration to be used.
     * @param userAuthenticationProvider  the authentication provider to be used for users.
     * @param adminAuthenticationProvider the authentication provider to be used for admins.
     * @return a ProviderManager configured with the userAuthenticationProvider and adminAuthenticationProvider beans.
     * @throws Exception if there is an error creating the ProviderManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration,
                                                       AuthenticationProvider userAuthenticationProvider,
                                                       AuthenticationProvider adminAuthenticationProvider) throws Exception {
        return new ProviderManager(List.of(userAuthenticationProvider, adminAuthenticationProvider));
    }

    /**
     * Returns a string representing the secret key used for JWT token signing and verification.
     * This bean is used to provide a secret key for JWT token generation and verification.
     *
     * @return a string representing the secret key used for JWT token signing and verification.
     */
    @Bean
    public String secretKey(){
        return secretKey;
    }
}
