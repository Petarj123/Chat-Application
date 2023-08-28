package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.*;
import com.auth.app.jwt.service.JwtService;
import com.auth.app.model.user.model.User;
import com.auth.app.model.user.repository.UserRepository;
import com.auth.app.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void registerUser(@RequestBody RegistrationRequest request) throws InvalidPasswordException, UnavailableEmailException, InvalidEmailException, PasswordMismatchException {
        authenticationService.registerUser(request);
    }

    
    @PostMapping("/authenticate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request){
        return authenticationService.authenticate(request.usernameOrEmail(), request.password());
    }

    
    @PostMapping("/recovery")
    @ResponseStatus(HttpStatus.OK)
    public void recoveryEmail(@RequestBody EmailRequest request) {
        authenticationService.passwordRecoveryEmail(request.email());
    }

    
    @PutMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestBody PasswordRequest request, @RequestParam("token") String resetToken) throws PasswordMismatchException, InvalidPasswordException, InvalidResetPasswordTokenException, InvalidUserException {
        User user = userRepository.findByResetPasswordToken(resetToken).orElseThrow(() -> new InvalidUserException("Could not find user."));
        authenticationService.resetPassword(user.getUsername(), resetToken, request.password(), request.confirmPassword());
    }
}
