package com.auth.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidEmailException extends Exception{
    
    public InvalidEmailException(String message){
        super(message);
    }
}
