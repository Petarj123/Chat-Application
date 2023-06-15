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


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@RequestHeader("Authorization") String header, @PathVariable("id") String userId) throws InvalidRoleException {
        String token = header.substring(7);
        adminService.deleteUser(token, userId);
    }

    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAdmin(@RequestBody RegistrationRequest request, @RequestHeader("Authorization") String header) throws InvalidPasswordException, InvalidEmailException, UnavailableEmailException {
        String token = header.substring(7);
        authenticationService.registerAdmin(request, token);
    }

}
