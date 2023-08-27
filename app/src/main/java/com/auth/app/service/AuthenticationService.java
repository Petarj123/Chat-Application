package com.auth.app.service;

import com.auth.app.DTO.AuthenticationResponse;
import com.auth.app.DTO.RegistrationRequest;
import com.auth.app.exceptions.*;
import com.auth.app.jwt.service.JwtService;
import com.auth.app.model.user.model.Role;
import com.auth.app.model.user.model.SecureUser;
import com.auth.app.model.user.model.User;
import com.auth.app.model.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSenderService mailSender;
    private static final String emailRegex = "^(.+)@(.+)$";
    public static final String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";


    public void registerUser(RegistrationRequest request) throws UnavailableEmailException, InvalidEmailException, InvalidPasswordException, PasswordMismatchException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UnavailableEmailException(request.email() + " is already registered");
        }
        validateEmail(request.email());
        validatePassword(request.password(), request.confirmPassword());

        User user = User
                .builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .chatRooms(new HashSet<>())
                .roles(new HashSet<>(List.of(Role.USER)))
                .createdAt(new Date())
                .build();
        userRepository.save(user);
        mailSender.sendRegistrationEmail(request.email());
    }

    public AuthenticationResponse authenticate(String usernameOrEmail, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                usernameOrEmail, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        manageUserRefreshToken(authentication);

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(authentication))
                .build();
    }

    
    @SneakyThrows
    public void passwordRecoveryEmail(String token) {
        String username = jwtService.getUsername(token);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found."));

        user.setResetPasswordToken(generateResetToken());

        userRepository.save(user);

        mailSender.sendRecoveryPasswordEmail(user.getEmail(), user.getResetPasswordToken());
    }


    public void resetPassword(String usernameOrEmail, String resetToken, String password, String confirmPassword) throws PasswordMismatchException, InvalidPasswordException, InvalidResetPasswordTokenException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() -> new UsernameNotFoundException("Could not find user."));

        if (!resetToken.equals(user.getResetPasswordToken())) {
            throw new InvalidResetPasswordTokenException("Reset password tokens do not match!");
        }
        validatePassword(password, confirmPassword);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
    }
    
    private String generateResetToken(){
        return UUID.randomUUID().toString();
    }
    private void validateEmail(String email) throws InvalidEmailException {
        if (!email.matches(emailRegex)) {
            throw new InvalidEmailException(email + " is not a valid email address");
        }
    }
    private void validatePassword(String password, String confirmPassword) throws InvalidPasswordException, PasswordMismatchException {
        if (!password.matches(passwordRegex)) {
            throw new InvalidPasswordException("Password must be at least 8 characters long and must contain at least one uppercase letter, one lowercase letter, one special character, and one number");
        } else if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match!");
        }
    }
    private void manageUserRefreshToken(Authentication authentication) {
        SecureUser secureUser = (SecureUser) authentication.getPrincipal();
        User user = userRepository.findByUsername(secureUser.getUsername()).orElseThrow();

        boolean shouldGenerateNewToken = false;

        if (user.getRefreshToken() == null || user.getRefreshToken().isEmpty()) {
            // No refresh token set
            shouldGenerateNewToken = true;
        } else {
            // Check if the token has expired
            try {
                if (!jwtService.validateRefreshToken(user.getRefreshToken())) {
                    Date refreshTokenExpiry = jwtService.extractExpirationDate(user.getRefreshToken());
                    if (refreshTokenExpiry.before(new Date())) {
                        shouldGenerateNewToken = true;  // Refresh token has expired
                    }
                }
            } catch (Exception e) {
                shouldGenerateNewToken = true;
            }
        }

        if (shouldGenerateNewToken) {
            String newRefreshToken = jwtService.generateRefreshToken(secureUser.getUsername());
            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);
        }
    }
}
