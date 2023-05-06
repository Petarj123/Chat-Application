package com.auth.app.service;

import com.auth.app.DTO.ChatRoomRequest;
import com.auth.app.DTO.InvitationRequest;
import com.auth.app.interfaces.ChatInterface;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Invitation;
import com.auth.app.model.InvitationStatus;
import com.auth.app.repository.ChatRoomRepository;
import com.auth.app.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ChatService implements ChatInterface {

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;
    private final InvitationRepository invitationRepository;

    @Override
    public void createChatRoom(ChatRoomRequest room, String token) {
        String creatorId = jwtService.extractId(token);
        ChatRoom chatRoom = ChatRoom.builder()
                .participantIds(Collections.singletonList(creatorId))
                .messages(new ArrayList<>())
                .type(room.type())
                .createdAt(new Date(System.currentTimeMillis()))
                .build();
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void sendInvitation(InvitationRequest invitationRequest, String token) {
        String userId = jwtService.extractId(token);
        Invitation invitation = Invitation.builder()
                .senderId(userId)
                .recipientId(invitationRequest.recipientId())
                .status(InvitationStatus.PENDING)
                .build();
        invitationRepository.save(invitation);

    }

    @Override
    public void acceptInvitation(InvitationRequest invitationRequest, String token) {

    }

    @Override
    public void declineInvitation(InvitationRequest invitationRequest, String token) {

    }
}
