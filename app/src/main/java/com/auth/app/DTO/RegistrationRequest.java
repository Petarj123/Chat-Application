package com.auth.app.DTO;


public record RegistrationRequest(String email, String password, String confirmPassword) {
}
