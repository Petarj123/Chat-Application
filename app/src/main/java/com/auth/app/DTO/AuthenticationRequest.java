package com.auth.app.DTO;


public record AuthenticationRequest(String usernameOrEmail, String password) {
}
