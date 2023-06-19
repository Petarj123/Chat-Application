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
        this.server.addEventListener("sendVoiceMessage", VoiceMessageRequest.class, this::handleSendVoiceMessage);
        this.server.addEventListener("createChatRoom", RoomNameRequest.class, this::handleCreateChatRoom);
        this.server.addEventListener("acceptInvite", InvitationRequest.class, this::handleAcceptInvite);
        this.server.addEventListener("createInvite", RoomRequest.class, this::handleCreateInvite);
        this.server.addEventListener("getParticipants", RoomRequest.class, this::handleGetParticipants);
        this.server.addEventListener("joinRoom", RoomRequest.class, this::handleJoinRoom);
        this.server.addEventListener("leaveRoom", RoomRequest.class, this::handleLeaveRoom);
    }

    private void handleCreateChatRoom(SocketIOClient client, RoomNameRequest request, AckRequest ackRequest) {
        final String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        String token = header.substring(7);
        chatService.createChatRoom(token, request.roomName());
        List<ChatRoom> chatRoomList = userService.getAllChatRooms(token);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(chatRoomList);
        }
    }

    private void handleSendMessage(SocketIOClient client, MessageRequest request, AckRequest ackRequest) throws ChatRoomException {
        final String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        String token = header.substring(7);
        Message message = chatService.sendMessage(request.roomId(), request.text(), token);

        this.server.getRoomOperations(request.roomId()).sendEvent("newMessage", message);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(message);
        }
    }
    private void handleSendVoiceMessage(SocketIOClient client, VoiceMessageRequest request, AckRequest ackRequest){
        final String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        String token = header.substring(7);

        Message message = chatService.sendVoiceMessage(request.roomId(), request.voiceMessage(), token);
        this.server.getRoomOperations(request.roomId()).sendEvent("newMessage", message);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(message);
        }
    }

    private void handleAcceptInvite(SocketIOClient client, InvitationRequest request, AckRequest ackRequest) throws InvalidInvitationException, ChatRoomException {
        final String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        String token = header.substring(7);
        chatService.acceptInvite(token, request.invitationLink());
        List<ChatRoom> chatRoomList = userService.getAllChatRooms(token);
        if (ackRequest.isAckRequested()){
            ackRequest.sendAckData(chatRoomList);
        }
    }
    private void handleCreateInvite(SocketIOClient client, RoomRequest request, AckRequest ackRequest){
        final String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        String token = header.substring(7);
        String invite = chatService.createInvite(token, request.roomId());
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(invite);
        }
    }
    private void handleGetParticipants(SocketIOClient client, RoomRequest request, AckRequest ackRequest) throws ChatRoomException, InvalidUserException {
        final String header = client.getHandshakeData().getUrlParams().get("token").get(0);
        String token = header.substring(7);
        List<String> participants = chatService.getParticipants(token, request.roomId());

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
}
