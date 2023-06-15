package com.auth.app.service;

import com.auth.app.exceptions.InvalidRoleException;
import com.auth.app.jwt.JwtService;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    
    public void deleteUser(String token, String userId) throws InvalidRoleException {
        if (isAdmin(token)){
            userRepository.deleteById(userId);
        } else throw new InvalidRoleException("User is not an admin");
    }
    
    private boolean isAdmin(String token){
        return !jwtService.isTokenExpired(token) && jwtService.extractRole(token).equals("MANAGER");
    }
}
