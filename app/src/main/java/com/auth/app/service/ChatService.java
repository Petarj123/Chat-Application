package com.auth.app.service;

import com.auth.app.DTO.InvitationRequest;
import com.auth.app.interfaces.ChatImpl;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.*;
import com.auth.app.repository.*;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService implements ChatImpl {

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final ChatRoomTopicRepository chatRoomTopicRepository;
    /*private final SimpMessagingTemplate simpMessageTemplate;*/
    private final MessageRepository messageRepository;

    @Override
    public void createChatRoom(String token) {
        String creatorId = jwtService.extractId(token);
        User user = userRepository.findByEmail(jwtService.extractEmail(token)).orElseThrow();
        ChatRoom chatRoom = ChatRoom.builder()
                .participantIds(new ArrayList<>())
                .messages(new ArrayList<>())
                .createdAt(new Date(System.currentTimeMillis()))
                .build();
        List<String> participants = chatRoom.getParticipantIds();
        participants.add(creatorId);
        chatRoom.setParticipantIds(participants);
        chatRoomRepository.save(chatRoom);

        List<String> chatRooms = user.getChatRooms();
        if (chatRooms == null) {
            chatRooms = new ArrayList<>();
        }
        chatRooms.add(chatRoom.getId());
        user.setChatRooms(chatRooms);
        userRepository.save(user);

        String chatRoomTopic = "/app/" + chatRoom.getId();
        ChatRoomTopic topic = ChatRoomTopic.builder()
                .chatRoomId(chatRoom.getId())
                .topic(chatRoomTopic)
                .build();
        chatRoomTopicRepository.save(topic);
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

    public void sendMessage(String roomId, String text, String token){
        String userId = jwtService.extractId(token);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        List<String> participants = chatRoom.getParticipantIds();
        List<Message> messages = chatRoom.getMessages();
        if (!participants.contains(userId)){
            throw new JwtException("User " + userId + "is not a participant");
        }
        Message message = Message.builder()
                .sender(userId)
                .text(text)
                .sentAt(new Date())
                .build();
        ChatRoomTopic topic = chatRoomTopicRepository.findByChatRoomId().orElseThrow();
        /*simpMessageTemplate.convertAndSend(topic.getTopic(), message);*/
        messages.add(message);
        chatRoom.setMessages(messages);

        messageRepository.save(message);
        chatRoomRepository.save(chatRoom);
    }
}
