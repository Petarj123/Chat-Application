package com.auth.test;
import com.auth.app.DTO.AuthenticationRequest;
import com.auth.app.DTO.AuthenticationResponse;
import com.auth.app.DTO.PasswordRequest;
import com.auth.app.DTO.RegistrationRequest;
import com.auth.app.exceptions.InvalidEmailException;
import com.auth.app.exceptions.InvalidPasswordException;
import com.auth.app.exceptions.UnavailableEmailException;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.Admin;
import com.auth.app.model.Role;
import com.auth.app.model.User;
import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import com.auth.app.service.AuthenticationService;
import com.auth.app.service.EmailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @InjectMocks
    private AuthenticationService authenticationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailSenderService mailSender;
    private RegistrationRequest registrationRequest;
    private AuthenticationRequest authenticationRequest;
    private PasswordRequest passwordRequest;
    private User user;
    private Admin admin;
    @BeforeEach
    public void setUp() {
        registrationRequest = new RegistrationRequest("test@example.com", "Password123!", "Password123!");
        authenticationRequest = new AuthenticationRequest("test@example.com", "Password123!");
        passwordRequest = new PasswordRequest("NewPassword123!", "NewPassword123!");
        user = User.builder()
                .email("test@example.com")
                .password("Password123!")
                .chatRooms(new ArrayList<>())
                .role(Role.USER)
                .createdAt(new Date())
                .build();
        admin = Admin.builder()
                .email("admin@example.com")
                .password("Password123!")
                .role(Role.ADMIN)
                .createdAt(new Date())
                .build();
    }
    @Test
    public void registerUser_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertDoesNotThrow(() -> authenticationService.registerUser(registrationRequest));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSender, times(1)).sendRegistrationEmail(anyString());
    }
    @Test
    public void registerUser_unavailableEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertThrows(UnavailableEmailException.class, () -> authenticationService.registerUser(registrationRequest));
        verify(userRepository, times(1)).findByEmail(anyString());
    }
    @Test
    public void registerUser_invalidEmail() {
        registrationRequest = new RegistrationRequest("invalidEmail", "Password123!", "Password123!");
        assertThrows(InvalidEmailException.class, () -> authenticationService.registerUser(registrationRequest));
    }
    @Test
    public void registerUser_invalidPassword() {
        registrationRequest = new RegistrationRequest("test@example.com", "password", "password");
        assertThrows(InvalidPasswordException.class, () -> authenticationService.registerUser(registrationRequest));
    }
    @Test
    public void registerAdmin_success() {
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);
        assertDoesNotThrow(() -> authenticationService.registerAdmin(registrationRequest, "validToken"));
        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(adminRepository, times(1)).save(any(Admin.class));
        verify(mailSender, times(1)).sendRegistrationEmail(anyString());
    }
    @Test
    public void authenticate_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(userRepository.save(any(User.class))).thenReturn(user);
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(adminRepository, times(1)).findByEmail(anyString());
        verify(jwtService, times(1)).generateToken(any(User.class));
    }
    @Test
    public void authenticate_failure() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(authenticationRequest));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(adminRepository, times(1)).findByEmail(anyString());
    }
    @Test
    public void passwordRecoveryEmail_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertDoesNotThrow(() -> authenticationService.passwordRecoveryEmail("test@example.com"));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSender, times(1)).sendRecoveryPasswordEmail(anyString(), anyString());
    }
    @Test
    public void passwordRecoveryEmail_failure() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(adminRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UnavailableEmailException.class, () -> authenticationService.passwordRecoveryEmail("test@example.com"));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(adminRepository, times(1)).findByEmail(anyString());
    }
    @Test
    public void resetPassword_success() {
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertDoesNotThrow(() -> authenticationService.resetPassword(resetToken, passwordRequest));
        verify(userRepository, times(1)).findByResetToken(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    public void resetPassword_invalidToken() {
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.empty());
        when(adminRepository.findByResetToken(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authenticationService.resetPassword("invalidToken", passwordRequest));
        verify(userRepository, times(1)).findByResetToken(anyString());
        verify(adminRepository, times(1)).findByResetToken(anyString());
    }
    @Test
    public void resetPassword_invalidPassword() {
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.of(user));
        passwordRequest = new PasswordRequest("password", "password");
        assertThrows(RuntimeException.class, () -> authenticationService.resetPassword(resetToken, passwordRequest));
        verify(userRepository, times(1)).findByResetToken(anyString());
    }
}