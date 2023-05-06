package com.auth.app.DTO;

/**
 * The type Registration request.
 */
public record RegistrationRequest(String email, String password, String confirmPassword) {
}
