package com.auth.app.controller;

import com.auth.app.jwt.JwtService;
import com.auth.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final ChatService chatService;
    private final JwtService jwtService;
    @GetMapping("/hi")
    @ResponseStatus(HttpStatus.OK)
    public String hello(){
        return "hi";
    }

    @PostMapping("/chat")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChatRoom(@RequestHeader("Authorization") String header){
        String token = header.substring(7);
        chatService.createChatRoom(token);
    }
}
