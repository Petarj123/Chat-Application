package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.exceptions.InvalidInvitationException;
import com.auth.app.exceptions.InvalidUserException;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Message;
import com.auth.app.service.ChatService;
import com.auth.app.service.UserService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SocketIOController {
    private final SocketIOServer server;
    private final ChatService chatService;
    private final UserService userService;

    public SocketIOController(SocketIOServer server, ChatService chatService, UserService userService) {
        this.server = server;
        this.chatService = chatService;
        this.userService = userService;

        this.server.addEventListener("sendMessage", MessageRequest.class, this::handleSendMessage);
        this.server.addEventListener("createChatRoom", RoomNameRequest.class, this::handleCreateChatRoom);
        this.server.addEventListener("acceptInvite", InvitationRequest.class, this::handleAcceptInvite);
        this.server.addEventListener("createInvite", RoomRequest.class, this::handleCreateInvite);
        this.server.addEventListener("getParticipants", RoomRequest.class, this::handleGetParticipants);
        this.server.addEventListener("joinRoom", RoomRequest.class, this::handleJoinRoom);
        this.server.addEventListener("leaveRoom", RoomRequest.class, this::handleLeaveRoom);
        this.server.addEventListener("kick", PromotionRequest.class, this::handleKickUserFromGroup);
        this.server.addEventListener("promote", PromotionRequest.class, this::handlePromoteToGroupAdmin);
        this.server.addEventListener("demote", PromotionRequest.class, this::handleDemoteGroupAdmin);
        this.server.addEventListener("getRole", PromotionRequest.class, this::handleGetGroupRole);
        this.server.addEventListener("leaveChatRoom", RoomRequest.class, this::handleLeaveChatRoom);
    }

    private void handleCreateChatRoom(SocketIOClient client, RoomNameRequest request, AckRequest ackRequest) {
        String token = getToken(client);
        chatService.createChatRoom(token, request.roomName());
        List<ChatRoom> chatRoomList = userService.getAllChatRooms(token);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(chatRoomList);
        }
    }

    private void handleSendMessage(SocketIOClient client, MessageRequest request, AckRequest ackRequest) throws ChatRoomException {
        String token = getToken(client);
        Message message = chatService.sendMessage(request.roomId(), request.text(), token);

        this.server.getRoomOperations(request.roomId()).sendEvent("newMessage", message);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(message);
        }
    }

    private void handleAcceptInvite(SocketIOClient client, InvitationRequest request, AckRequest ackRequest) throws InvalidInvitationException, ChatRoomException {
        String token = getToken(client);
        chatService.acceptInvite(token, request.invitationLink());
        List<ChatRoom> chatRoomList = userService.getAllChatRooms(token);
        if (ackRequest.isAckRequested()){
            ackRequest.sendAckData(chatRoomList);
        }
    }
    private void handleCreateInvite(SocketIOClient client, RoomRequest request, AckRequest ackRequest){
        String token = getToken(client);
        String invite = chatService.createInvite(token, request.roomId());
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(invite);
        }
    }
    private void handleGetParticipants(SocketIOClient client, RoomRequest request, AckRequest ackRequest) throws ChatRoomException, InvalidUserException {
        String token = getToken(client);
        Map<String, String> participants = chatService.getParticipants(token, request.roomId());

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(participants);
        }
    }
    private void handleJoinRoom(SocketIOClient client, RoomRequest request, AckRequest ackRequest) {
        client.joinRoom(request.roomId());

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData("Joined room " + request.roomId());
        }
    }

    private void handleLeaveRoom(SocketIOClient client, RoomRequest request, AckRequest ackRequest) {
        client.leaveRoom(request.roomId());

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData("Left room " + request.roomId());
        }
    }
    private void handleKickUserFromGroup(SocketIOClient client, PromotionRequest request, AckRequest ackRequest) throws ChatRoomException, InvalidUserException {
        String token = getToken(client);
        Map<String, String> remainingParticipants = chatService.kickUserFromGroup(token, request.roomId(), request.userId());

        if (ackRequest.isAckRequested()){
            ackRequest.sendAckData(remainingParticipants);
        }
    }
    private void handleDemoteGroupAdmin(SocketIOClient client, PromotionRequest request, AckRequest ack) throws ChatRoomException, InvalidUserException {
        String token = getToken(client);
        Map<String, String> participants = chatService.demoteGroupAdmin(token, request.roomId(), request.userId());

        if (ack.isAckRequested()){
            ack.sendAckData(participants);
        }
    }
    private void handlePromoteToGroupAdmin(SocketIOClient client, PromotionRequest request, AckRequest ack) throws ChatRoomException, InvalidUserException {
        String token = getToken(client);
        Map<String, String> participants = chatService.promoteToGroupAdmin(token, request.roomId(), request.userId());

        if (ack.isAckRequested()){
            ack.sendAckData(participants);
        }
    }
    private void handleGetGroupRole(SocketIOClient client, PromotionRequest request, AckRequest ack) throws ChatRoomException, InvalidUserException {
        String token = getToken(client);
        Map<String, String> emailAndRole = chatService.getGroupRole(token, request.roomId());

        if (ack.isAckRequested()){
            ack.sendAckData(emailAndRole);
        }
    }
    private void handleLeaveChatRoom(SocketIOClient client, RoomRequest request, AckRequest ack) throws ChatRoomException, InvalidUserException {
        String token = getToken(client);
        List<ChatRoom> rooms = chatService.leaveChatRoom(token, request.roomId());

        if (ack.isAckRequested()){
            ack.sendAckData(rooms);
        }
    }
    private String getToken(SocketIOClient client){
        String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        return header.substring(7);
    }
}
