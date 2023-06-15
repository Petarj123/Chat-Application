package com.auth.app.config;

import com.auth.app.model.Role;
import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomUserDetailsService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    
    public UserDetails loadUserByUsernameAndRole(String username, String role) throws UsernameNotFoundException {
        if (Role.USER.name().equals(role)) {
            return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } else if (Role.ADMIN.name().equals(role)) {
            return adminRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        }
        throw new IllegalArgumentException("Invalid role provided");
    }


}
