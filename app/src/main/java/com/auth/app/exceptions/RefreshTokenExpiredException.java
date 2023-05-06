package com.auth.app.exceptions;

/**
 * The type Refresh token expired exception.
 */
public class RefreshTokenExpiredException extends Throwable {
    /**
     * Instantiates a new Refresh token expired exception.
     *
     * @param message the message
     */
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
