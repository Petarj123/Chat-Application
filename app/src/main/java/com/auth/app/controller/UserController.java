package com.auth.app.controller;

import com.auth.app.DTO.InvitationRequest;
import com.auth.app.model.ChatRoom;
import com.auth.app.service.ChatService;
import com.auth.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ChatService chatService;
    @GetMapping("/allChats")
    @ResponseStatus(HttpStatus.OK)
    public List<ChatRoom> getAllChats(@RequestHeader("Authorization") String token) {
        return userService.getAllChatRooms(token);
    }
    @PostMapping("/createRoom")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChatRoom(@RequestHeader("Authorization") String token){
        chatService.createChatRoom(token);
    }

}
