package com.auth.app.service;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.UnavailableEmailException;
import com.auth.app.exceptions.InvalidEmailException;
import com.auth.app.exceptions.InvalidPasswordException;
import com.auth.app.model.User;
import com.auth.app.repository.AdminRepository;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.Admin;
import com.auth.app.model.Role;
import com.auth.app.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSenderService mailSender;
    private static final String emailRegex = "^(.+)@(.+)$";
    public static final String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";


    public void registerUser(RegistrationRequest request) throws UnavailableEmailException, InvalidEmailException, InvalidPasswordException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UnavailableEmailException(request.email() + " is already registered");
        }
        validateEmail(request.email());
        validatePassword(request.password(), request.confirmPassword());

        User user = User
                .builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .chatRooms(new ArrayList<>())
                .role(Role.USER)
                .createdAt(new Date())
                .build();
        userRepository.save(user);
        mailSender.sendRegistrationEmail(request.email());
    }


    public void registerAdmin(RegistrationRequest request, String token) throws InvalidPasswordException, InvalidEmailException, UnavailableEmailException {
        if (adminRepository.findByEmail(request.email()).isPresent() && isAdmin(token)) {
            throw new UnavailableEmailException(request.email() + " is already registered");
        }
        validateEmail(request.email());
        validatePassword(request.password(), request.confirmPassword());
        Admin manager = Admin
                .builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .createdAt(new Date())
                .build();
        adminRepository.save(manager);
        mailSender.sendRegistrationEmail(request.email());
    }

    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.email()).orElse(null);
        var admin = adminRepository.findByEmail(request.email()).orElse(null);
        if (user != null) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            var jwtToken = jwtService.generateToken(user);
            if (user.getRefreshToken() == null || user.getRefreshToken().isEmpty() || !jwtService.isTokenValid(user.getRefreshToken(), user)){
                String refreshToken = jwtService.generateRefreshToken(user);
                user.setRefreshToken(refreshToken);
                userRepository.save(user);
            }
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        } else if (admin != null){
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            var jwtToken = jwtService.generateToken(admin);
            if (admin.getRefreshToken() == null || admin.getRefreshToken().isEmpty() || !jwtService.isTokenValid(admin.getRefreshToken(), admin)){
                String refreshToken = jwtService.generateRefreshToken(admin);
                admin.setRefreshToken(refreshToken);
                adminRepository.save(admin);
            }
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        } else {
            throw new UsernameNotFoundException(request.email() + " does not exist");
        }
    }

    
    @SneakyThrows
    public void passwordRecoveryEmail(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        Optional<Admin> existingAdmin = adminRepository.findByEmail(email);
        ObjectMapper mapper = new ObjectMapper();
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            String resetToken = generateResetToken();
            user.setResetToken(resetToken);
            userRepository.save(user);
            mailSender.sendRecoveryPasswordEmail(email, resetToken);

        } else if (existingAdmin.isPresent()) {
            Admin admin = existingAdmin.get();
            String resetToken = generateResetToken();
            admin.setResetToken(resetToken);
            adminRepository.save(admin);
            mailSender.sendRecoveryPasswordEmail(email, resetToken);

        } else throw new UnavailableEmailException("Email address does not exist");
    }

    
    public void resetPassword(String resetToken, PasswordRequest request) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

        Optional<User> existingUser = userRepository.findByResetToken(resetToken);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (request.password().equals(request.confirmPassword()) && request.password().matches(passwordRegex)) {
                user.setPassword(passwordEncoder.encode(request.password()));
                user.setResetToken(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Passwords don't match or don't meet requirements");
            }
        } else {
            Optional<Admin> existingAdmin = adminRepository.findByResetToken(resetToken);
            if (existingAdmin.isPresent()) {
                Admin admin = existingAdmin.get();
                if (request.password().equals(request.confirmPassword()) && request.password().matches(passwordRegex)) {
                    admin.setPassword(passwordEncoder.encode(request.password()));
                    admin.setResetToken(null);
                    adminRepository.save(admin);
                } else {
                    throw new RuntimeException("Passwords don't match or don't meet requirements");
                }
            } else {
                throw new RuntimeException("Invalid token");
            }
        }
    }

    
    private String generateResetToken(){
        return UUID.randomUUID().toString();
    }
    private void validateEmail(String email) throws InvalidEmailException {
        if (!email.matches(emailRegex)) {
            throw new InvalidEmailException(email + " is not a valid email address");
        }
    }
    private void validatePassword(String password, String confirmPassword) throws InvalidPasswordException {
        if (!password.matches(passwordRegex)) {
            throw new InvalidPasswordException("Password must be at least 8 characters long and must contain at least one uppercase letter, one lowercase letter, one special character, and one number");
        } else if (!password.equals(confirmPassword)) {
            throw new InvalidPasswordException("Passwords do not match!");
        }
    }
    public boolean isAdmin(String token) {
        return jwtService.isTokenExpired(token) && jwtService.extractRole(token).equals("ADMIN");
    }
}
