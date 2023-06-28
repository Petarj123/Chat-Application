package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.*;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Message;
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
    public List<ChatRoom> getAllChats(@RequestHeader("Authorization") String header) {
        return userService.getAllChatRooms(getToken(header));
    }
    @PostMapping("/allMessages")
    @ResponseStatus(HttpStatus.OK)
    public List<Message> getAllMessages(@RequestHeader("Authorization") String header, @RequestBody RoomRequest request) throws ChatRoomException {
        return userService.getAllMessages(getToken(header), request.roomId());
    }
    @PostMapping("/createRoom")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChatRoom(@RequestHeader("Authorization") String header, @RequestBody RoomNameRequest request) {
        chatService.createChatRoom(getToken(header), request.roomName());
    }
    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public String createInvite(@RequestHeader("Authorization") String header, @RequestBody RoomRequest request){
        return chatService.createInvite(getToken(header), request.roomId());
    }
    @PostMapping("/acceptInvite")
    @ResponseStatus(HttpStatus.OK)
    public void acceptInvite(@RequestHeader("Authorization") String header, @RequestBody InvitationRequest request) throws InvalidInvitationException, ChatRoomException {
        chatService.acceptInvite(getToken(header), request.invitationLink());
    }
    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@RequestHeader("Authorization") String header, @RequestBody ChangePasswordRequest request) throws InvalidPasswordException, InvalidUserException {
        userService.changePassword(getToken(header), request.oldPassword(), request.newPassword());
    }
    @PutMapping("/change-email")
    @ResponseStatus(HttpStatus.OK)
    public void changeEmail(@RequestHeader("Authorization") String header, @RequestBody EmailRequest request) throws InvalidEmailException {
        userService.changeEmail(getToken(header), request.email());
    }
    @PostMapping("/leave-chat")
    @ResponseStatus(HttpStatus.OK)
    public void leaveChatRoom(@RequestHeader("Authorization") String header, @RequestParam String roomId) throws ChatRoomException, InvalidUserException {
        userService.leaveChatRoom(getToken(header), roomId);
    }
    @PutMapping("/grant-group-admin")
    @ResponseStatus(HttpStatus.OK)
    public void grantGroupAdminRole(@RequestHeader("Authorization") String header, @RequestBody PromotionRequest request) throws ChatRoomException, InvalidUserException {
        chatService.promoteToGroupAdmin(getToken(header), request.roomId(), request.userId());
    }
    @PutMapping("/revoke-group-admin")
    @ResponseStatus(HttpStatus.OK)
    public void revokeGroupAdminRole(@RequestHeader("Authorization") String header, @RequestBody PromotionRequest request) throws InvalidUserException, ChatRoomException {
        chatService.demoteGroupAdmin(getToken(header), request.roomId(), request.userId());
    }
    @PostMapping("/kick-user")
    @ResponseStatus(HttpStatus.OK)
    public void kickUser(@RequestHeader("Authorization") String header, @RequestBody PromotionRequest request) throws InvalidUserException, ChatRoomException {
        chatService.kickUserFromGroup(getToken(header), request.roomId(), request.userId());
    }


    private String getToken(String header){
        return header.substring(7);
    }
}
