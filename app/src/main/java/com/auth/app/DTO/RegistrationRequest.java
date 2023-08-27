package com.auth.app.DTO;


public record RegistrationRequest(String username, String email, String password, String confirmPassword) {
}
