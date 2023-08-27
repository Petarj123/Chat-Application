package com.auth.app.exceptions;

public class ExpiredRefreshTokenException extends Exception{

    public ExpiredRefreshTokenException(String message) {
        super(message);
    }
}
