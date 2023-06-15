package com.auth.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ChatRoomException extends Exception {
    public ChatRoomException(String message){
        super(message);
    }
}
