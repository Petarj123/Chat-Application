package com.auth.app.exceptions;

public class InvalidResetPasswordTokenException extends Exception {
    public InvalidResetPasswordTokenException(String message) {
        super(message);
    }
}
