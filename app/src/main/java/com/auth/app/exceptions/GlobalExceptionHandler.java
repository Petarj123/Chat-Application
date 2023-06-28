package com.auth.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnavailableEmailException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleUnavailableEmailException(UnavailableEmailException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }

    @ExceptionHandler(InvalidEmailException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleInvalidEmailException(InvalidEmailException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleInvalidPasswordException(InvalidPasswordException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }
    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleInvalidRoleException(InvalidRoleException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleInvalidTokenException(InvalidTokenException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }
    @ExceptionHandler(InvalidInvitationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleInvalidInvitationException(InvalidInvitationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }
    @ExceptionHandler(InvalidUserException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleInvalidUserException(InvalidUserException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }
    @ExceptionHandler(RefreshTokenExpiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleRefreshTokenException(RefreshTokenExpiredException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }
    @ExceptionHandler(ChatRoomException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleChatRoomException(ChatRoomException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }

}

