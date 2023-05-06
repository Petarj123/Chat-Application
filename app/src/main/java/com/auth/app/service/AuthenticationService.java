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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Authentication service.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSenderService mailSender;

    /**
     * Registers a new user with the given information and saves their details to the database.
     *
     * @param request the registration request containing the user's email and password
     * @throws UnavailableEmailException if the provided email is already registered with the system
     * @throws InvalidEmailException     if the provided email is not a valid email address
     * @throws InvalidPasswordException  if the provided password does not meet the password requirements
     */
    public void registerUser(RegistrationRequest request) throws UnavailableEmailException, InvalidEmailException, InvalidPasswordException {
        String emailRegex = "^(.+)@(.+)$";
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UnavailableEmailException(request.email() + " is already registered");
        } else if (!request.email().matches(emailRegex)) {
            throw new InvalidEmailException(request.email() + " is not a valid email address");
        }
        if (!request.password().matches(passwordRegex)) {
            throw new InvalidPasswordException("Password must be at least 8 characters long and must contain at least one uppercase letter, one lowercase letter, one special character, and one number");
        } else if (!request.password().matches(request.confirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match!");
        }
        User user = User
                .builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .createdAt(new Date())
                .build();
        userRepository.save(user);
        mailSender.sendRegistrationEmail(request.email());
    }

    /**
     * Registers a new admin with the given information and saves their details to the database.
     *
     * @param request the registration request containing the admin's email and password
     * @param token   the authentication token of the manager who is performing the registration
     * @throws InvalidPasswordException  if the provided password does not meet the password requirements
     * @throws InvalidEmailException     if the provided email is not a valid email address
     * @throws UnavailableEmailException if the provided email is already registered with the system
     */
    public void registerAdmin(RegistrationRequest request, String token) throws InvalidPasswordException, InvalidEmailException, UnavailableEmailException {
        String emailRegex = "^(.+)@(.+)$";
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

        if (adminRepository.findByEmail(request.email()).isPresent() && isAdmin(token)) {
            throw new UnavailableEmailException(request.email() + " is already registered");
        } else if (!request.email().matches(emailRegex)) {
            throw new InvalidEmailException(request.email() + " is not a valid email address");
        }
        if (!request.password().matches(passwordRegex)) {
            throw new InvalidPasswordException("Password must be at least 8 characters long and must contain at least one uppercase letter, one lowercase letter, one special character, and one number");
        } else if (!request.password().matches(request.confirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match!");
        }
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

    /**
     * Authenticates a user or admin with the provided email and password, and generates a JWT token if the authentication is successful.
     *
     * @param request the authentication request containing the email and password of the user/admin
     * @return an authentication response containing the JWT token
     * @throws UsernameNotFoundException if no user or admin with the provided email exists
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        if (userRepository.findByEmail(request.email()).isPresent()) {
            var user = userRepository.findByEmail(request.email()).orElseThrow();
            var jwtToken = jwtService.generateToken(user);
            if (user.getRefreshToken() == null || user.getRefreshToken().isEmpty() || !jwtService.isTokenValid(user.getRefreshToken(), user)){
                String refreshToken = jwtService.generateRefreshToken(user);
                user.setRefreshToken(refreshToken);
                userRepository.save(user);
            }
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();

        } else if (adminRepository.findByEmail(request.email()).isPresent()){
            var admin = adminRepository.findByEmail(request.email()).orElseThrow();
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

    /**
     * Sends a password recovery email to the user or admin with the given email address, containing a reset token for their password.
     *
     * @param email the email address of the user or admin
     * @throws UnavailableEmailException if no user or admin with the provided email exists
     */
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

    /**
     * Resets the password of a user or admin with the provided reset token, and saves the new password to the database.
     *
     * @param resetToken the reset token for the user or admin's password
     * @param request    the password request containing the new password and confirmation password
     * @throws RuntimeException if the new password and confirmation password do not match or do not meet the password requirements, or if the reset token is invalid
     */
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

    /**
     * Generates a new reset token for use in password recovery.
     *
     * @return the generated reset token
     */
    private String generateResetToken(){
        return UUID.randomUUID().toString();
    }

    /**
     * Checks whether the authenticated user is an admin.
     *
     * @param token the JWT token of the authenticated user
     * @return true if the user is an admin, false otherwise
     */
    private boolean isAdmin(String token) {
        return jwtService.isTokenExpired(token) && jwtService.extractRole(token).equals("ADMIN");
    }
}
