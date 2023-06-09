package com.auth.app.controller;

import com.auth.app.DTO.MessageRequest;
import com.auth.app.service.ChatService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;

@Component
public class SocketIOController {
    private final SocketIOServer server;
    private final ChatService chatService;

    public SocketIOController(SocketIOServer server, ChatService chatService) {
        this.server = server;
        this.chatService = chatService;

        this.server.addEventListener("sendMessage", MessageRequest.class, this::handleSendMessage);
    }
    private void handleSendMessage(SocketIOClient client, MessageRequest request, AckRequest ackRequest) {
        String header = client.getHandshakeData().getHttpHeaders().get("Authorization");
        String token = header.substring(7);
        chatService.sendMessage(request.roomId(), request.text(), token);
    }
}
