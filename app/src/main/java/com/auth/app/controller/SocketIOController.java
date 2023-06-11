package com.auth.app.controller;

import com.auth.app.DTO.MessageRequest;
import com.auth.app.DTO.RoomNameRequest;
import com.auth.app.exceptions.ChatRoomException;
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
        this.server.addEventListener("createChatRoom", RoomNameRequest.class, this::handleCreateChatRoom);
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
        chatService.sendMessage(request.roomId(), request.text(), token);
        List<Message> messages = userService.getAllMessages(token, request.roomId());

        if (ackRequest.isAckRequested()){
            ackRequest.sendAckData(messages);
        }
    }
}
