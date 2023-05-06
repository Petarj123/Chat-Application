package com.auth.app.exceptions;

/**
 * The type Invalid password exception.
 */
public class InvalidPasswordException extends Exception{
    /**
     * Instantiates a new Invalid password exception.
     *
     * @param message the message
     */
    public InvalidPasswordException(String message){
        super(message);
    }
}
