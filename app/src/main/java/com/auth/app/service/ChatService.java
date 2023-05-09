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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
                .chatroomId(invitationRequest.chatroomId())
                .status(InvitationStatus.PENDING)
                .build();
        invitationRepository.save(invitation);

    }

    @Override
    public void acceptInvitation(String invitationId, String token) {
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();
        String userId = jwtService.extractId(token);

        if (userId.equals(invitation.getRecipientId())){

            ChatRoom chatRoom = chatRoomRepository.findById(invitation.getChatroomId()).orElseThrow();
            List<String> participants = chatRoom.getParticipantIds();
            participants.add(invitation.getRecipientId());
            invitation.setStatus(InvitationStatus.INVITATION_ACCEPTED);

            invitationRepository.save(invitation);
            chatRoomRepository.save(chatRoom);
        }
    }


    @Override
    public void declineInvitation(String invitationId, String token) {
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();
        String userId = jwtService.extractId(token);

        if (userId.equals(invitation.getRecipientId())){
            invitation.setStatus(InvitationStatus.INVITATION_DECLINED);
            invitationRepository.save(invitation);
        }
    }
}
