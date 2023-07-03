package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.InvalidEmailException;
import com.auth.app.exceptions.InvalidPasswordException;
import com.auth.app.exceptions.UnavailableEmailException;
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

    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void registerUser(@RequestBody RegistrationRequest request) throws InvalidPasswordException, UnavailableEmailException, InvalidEmailException {
        authenticationService.registerUser(request);
    }

    
    @PostMapping("/authenticate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request){
        return authenticationService.authenticate(request);
    }

    
    @PostMapping("/recovery")
    @ResponseStatus(HttpStatus.OK)
    public void recoveryEmail(@RequestBody EmailRequest request) throws JsonProcessingException {
        authenticationService.passwordRecoveryEmail(request.email());
    }

    
    @PutMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestParam("token") String resetToken, @RequestBody PasswordRequest request){
        authenticationService.resetPassword(resetToken, request);
    }
}
