package com.auth.app.controller;

import com.auth.app.DTO.RegistrationRequest;
import com.auth.app.exceptions.InvalidEmailException;
import com.auth.app.exceptions.InvalidPasswordException;
import com.auth.app.exceptions.InvalidRoleException;
import com.auth.app.exceptions.UnavailableEmailException;
import com.auth.app.service.AdminService;
import com.auth.app.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * The type Admin controller.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    /**
     * Deletes a user with the given user ID.
     * The method requires an authentication token to be passed in the "Authorization" header.
     * The method passes the authentication token and user ID to the adminService to handle user deletion.
     *
     * @param header the authorization token in the "Authorization" header.
     * @param userId the ID of the user to be deleted.
     * @throws InvalidRoleException if the authenticated user is not an admin
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@RequestHeader("Authorization") String header, @PathVariable("id") String userId) throws InvalidRoleException {
        String token = header.substring(7);
        adminService.deleteUser(token, userId);
    }

    /**
     * Registers a new admin with the given registration request.
     * The registration request includes the admin's email, username, and password.
     * The method requires an authentication token to be passed in the "Authorization" header.
     * The method passes the registration request and authentication token to the authenticationService to handle admin registration.
     *
     * @param request the registration request that contains the admin's email, username, and password.
     * @param header  the authorization token in the "Authorization" header.
     * @throws InvalidPasswordException  if the password does not meet the security requirements.
     * @throws InvalidEmailException     if the email is not in a valid format.
     * @throws UnavailableEmailException if the email is already associated with an existing account.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@RequestBody RegistrationRequest request, @RequestHeader("Authorization") String header) throws InvalidPasswordException, InvalidEmailException, UnavailableEmailException {
        String token = header.substring(7);
        authenticationService.registerAdmin(request, token);
    }

}
