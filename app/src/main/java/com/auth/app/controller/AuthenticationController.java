package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.*;
import com.auth.app.jwt.service.JwtService;
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
    public void recoveryEmail(@RequestHeader("Authorization") String header) throws JsonProcessingException {
        String token = header.substring(7);
        authenticationService.passwordRecoveryEmail(token);
    }

    
    @PutMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestBody PasswordRequest request,@RequestParam("resetToken") String resetToken, @RequestHeader("Authorization") String header) throws PasswordMismatchException, InvalidPasswordException, InvalidResetPasswordTokenException {
        String token = header.substring(7);
        String username = jwtService.getUsername(token);

        authenticationService.resetPassword(username, resetToken, request.password(), request.confirmPassword());
    }
}
