package com.auth.app.config;

import com.auth.app.model.Role;
import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * The type Custom user details service.
 */
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    /**
     * This method is used to load user details by their username and role.
     * If the role is "USER", the method looks up the user in the userRepository by their email.
     * If the role is "ADMIN", the method looks up the admin in the adminRepository by their email.
     * If the role is not valid, an IllegalArgumentException is thrown.
     * This method is typically used in authentication processes to retrieve the user or admin's details for authentication.
     *
     * @param username the email of the user or admin.
     * @param role     the role of the user or admin ("USER" or "ADMIN").
     * @return a UserDetails object containing the user or admin's details.
     * @throws UsernameNotFoundException if the user or admin is not found.
     */
    public UserDetails loadUserByUsernameAndRole(String username, String role) throws UsernameNotFoundException {
        if (Role.USER.name().equals(role)) {
            return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } else if (Role.ADMIN.name().equals(role)) {
            return adminRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        }
        throw new IllegalArgumentException("Invalid role provided");
    }


}
