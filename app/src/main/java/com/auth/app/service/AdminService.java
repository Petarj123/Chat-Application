package com.auth.app.service;

import com.auth.app.exceptions.InvalidRoleException;
import com.auth.app.jwt.JwtService;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The type Admin service.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Deletes the user with the given ID if the authenticated user is an admin.
     * Throws a RuntimeException if the authenticated user is not an admin.
     *
     * @param token  the JWT token of the authenticated user
     * @param userId the ID of the user to be deleted
     * @throws InvalidRoleException if the authenticated user is not an admin
     */
    public void deleteUser(String token, String userId) throws InvalidRoleException {
        if (isAdmin(token)){
            userRepository.deleteById(userId);
        } else throw new InvalidRoleException("User is not an admin");
    }
    /**
     * Checks whether the authenticated user is an admin or not.
     *
     * @param token the JWT token of the authenticated user
     * @return true if the authenticated user is an admin, false otherwise
     */
    private boolean isAdmin(String token){
        return !jwtService.isTokenExpired(token) && jwtService.extractRole(token).equals("MANAGER");
    }
}
