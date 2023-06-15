package com.auth.app.config;

import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
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
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    @Value("${secret.key}")
    private String secretKey;

    
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public UserDetailsService userDetailsService(){
        UsernameNotFoundException notFoundException = new UsernameNotFoundException("User does not exist");
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> notFoundException);
    }
    @Bean
    public UserDetailsService adminDetailsService(){
        UsernameNotFoundException notFoundException = new UsernameNotFoundException("Admin does not exist");
        return email -> adminRepository.findByEmail(email)
                .orElseThrow(() -> notFoundException);
    }

    
    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    
    @Bean
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(adminDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration,
                                                       AuthenticationProvider userAuthenticationProvider,
                                                       AuthenticationProvider adminAuthenticationProvider) throws Exception {
        List<AuthenticationProvider> providers = Arrays.asList(userAuthenticationProvider, adminAuthenticationProvider);
        return new ProviderManager(providers);
    }

    
    @Bean
    public String secretKey(){
        return secretKey;
    }

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }
}
