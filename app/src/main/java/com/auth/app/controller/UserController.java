package com.auth.app.controller;

import com.auth.app.DTO.InvitationRequest;
import com.auth.app.DTO.RoomNameRequest;
import com.auth.app.DTO.RoomRequest;
import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.exceptions.InvalidInvitationException;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Message;
import com.auth.app.service.ChatService;
import com.auth.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    public List<ChatRoom> getAllChats(@RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        return userService.getAllChatRooms(token);
    }
    @PostMapping("/allMessages")
    @ResponseStatus(HttpStatus.OK)
    public List<Message> getAllMessages(@RequestHeader("Authorization") String header, @RequestBody RoomRequest request) throws ChatRoomException {
        String token = header.substring(7);
        return userService.getAllMessages(token, request.roomId());
    }
    @PostMapping("/createRoom")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChatRoom(@RequestHeader("Authorization") String header, @RequestBody RoomNameRequest request) {
        String token = header.substring(7);
        chatService.createChatRoom(token, request.roomName());
    }
    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public String createInvite(@RequestHeader("Authorization") String header, @RequestBody RoomRequest request){
        String token = header.substring(7);
        return chatService.createInvite(token, request.roomId());
    }
    @PostMapping("/acceptInvite")
    @ResponseStatus(HttpStatus.OK)
    public void acceptInvite(@RequestHeader("Authorization") String header, @RequestBody InvitationRequest request) throws InvalidInvitationException, ChatRoomException {
        String token = header.substring(7);
        chatService.acceptInvite(token, request.invitationLink());
    }

}
