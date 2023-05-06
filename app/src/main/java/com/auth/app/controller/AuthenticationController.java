package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.InvalidEmailException;
import com.auth.app.exceptions.InvalidPasswordException;
import com.auth.app.exceptions.UnavailableEmailException;
import com.auth.app.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

/**
 * The type Authentication controller.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Registers a new user with the given registration request.
     * The registration request includes the user's email, username, and password.
     * The method passes the request to the authenticationService to handle user registration.
     *
     * @param request the registration request that contains the user's email, username, and password.
     * @throws InvalidPasswordException  if the password does not meet the security requirements.
     * @throws UnavailableEmailException if the email is already associated with an existing account.
     * @throws InvalidEmailException     if the email is not in a valid format.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@RequestBody RegistrationRequest request) throws InvalidPasswordException, UnavailableEmailException, InvalidEmailException {
        authenticationService.registerUser(request);
    }

    /**
     * Authenticates a user with the given authentication request.
     * The authentication request includes the user's email and password.
     * The method passes the request to the authenticationService to handle authentication.
     * If the authentication is successful, an AuthenticationResponse is returned with a JWT token.
     *
     * @param request the authentication request that contains the user's email and password.
     * @return an AuthenticationResponse that contains the JWT token if the authentication is successful.
     * @throws AuthenticationServiceException if there is an error authenticating the user.
     */
    @PostMapping("/authenticate")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request){
        return authenticationService.authenticate(request);
    }

    /**
     * Sends a password recovery email to the user with the given email.
     * The method passes the email to the authenticationService to handle the password recovery email.
     *
     * @param request the email request that contains the user's email.
     * @throws JsonProcessingException if there is an error converting the email to JSON format.
     */
    @PostMapping("/recovery")
    @ResponseStatus(HttpStatus.OK)
    public void recoveryEmail(@RequestBody EmailRequest request) throws JsonProcessingException {
        authenticationService.passwordRecoveryEmail(request.email());
    }

    /**
     * Resets the password for a user with the given reset token and password request.
     * The reset token is passed as a query parameter and the password request contains the new password.
     * The method passes the reset token and password request to the authenticationService to handle password reset.
     *
     * @param resetToken the reset token that was sent to the user's email.
     * @param request    the password request that contains the new password.
     * @throws AuthenticationServiceException if there is an error resetting the user's password.
     */
    @PutMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestParam("token") String resetToken, @RequestBody PasswordRequest request){
        authenticationService.resetPassword(resetToken, request);
    }
}
