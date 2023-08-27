package com.auth.app.exceptions;

public class PasswordMismatchException extends Exception{
    public PasswordMismatchException(String message){
        super(message);
    }
}
