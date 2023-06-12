package com.auth.app.service;

import com.auth.app.DTO.RoomNameRequest;
import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.exceptions.InvalidInvitationException;
import com.auth.app.exceptions.InvalidUserException;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.*;
import com.auth.app.repository.*;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;
    private final InvitationRepository invitationRepository;
    private final ChatRoomTopicRepository chatRoomTopicRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public void createChatRoom(String token, String roomName) {
        String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .participantIds(new ArrayList<>())
                .messages(new ArrayList<>())
                .createdAt(new Date())
                .build();
        List<String> participants = chatRoom.getParticipantIds();
        participants.add(userId);
        chatRoom.setParticipantIds(participants);
        chatRoomRepository.save(chatRoom);

        List<String> userChatRooms = user.getChatRooms();
        userChatRooms.add(chatRoom.getId());
        user.setChatRooms(userChatRooms);

        userRepository.save(user);

    }


    public String createInvite(String token, String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        String userId = jwtService.extractId(token);

        if (!chatRoom.getParticipantIds().contains(userId)){
            throw new RuntimeException("User " + userId + "is not in chat room");
        }
        Invitation invitation = Invitation.builder()
                .senderId(userId)
                .chatroomId(roomId)
                .invitationLink(generateInvitationLink())
                .createdAt(new Date())
                .isExpired(false)
                .build();
        invitationRepository.save(invitation);
        return invitation.getInvitationLink();
    }
    public void acceptInvite(String token, String invitationLink) throws InvalidInvitationException, ChatRoomException {
        String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow();
        if (isInvitationValid(invitationLink)){
            Invitation invitation = invitationRepository.findByInvitationLink(invitationLink).orElseThrow();
            ChatRoom chatRoom = chatRoomRepository.findById(invitation.getChatroomId()).orElseThrow();
            List<String> chatRoomParticipants = chatRoom.getParticipantIds();
            List<String> userChatRooms = user.getChatRooms();
            if (chatRoomParticipants.contains(userId)){
                throw new ChatRoomException("User is already part of this room");
            }
            chatRoomParticipants.add(userId);

            chatRoom.setParticipantIds(chatRoomParticipants);
            chatRoomRepository.save(chatRoom);

            invitation.setExpired(true);
            invitationRepository.save(invitation);

            userChatRooms.add(chatRoom.getId());
            userRepository.save(user);
        }
    }

    public void sendMessage(String roomId, String text, String token){
        String userId = jwtService.extractId(token);
        String username = jwtService.extractEmail(token);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        List<String> participants = chatRoom.getParticipantIds();
        List<Message> messages = chatRoom.getMessages();

        if (!participants.contains(userId)){
            throw new JwtException("User " + userId + "is not a participant");
        }
        Message message = Message.builder()
                .sender(username)
                .text(text)
                .sentAt(new Date())
                .build();
        messageRepository.save(message);

        messages.add(message);
        chatRoom.setMessages(messages);

        chatRoomRepository.save(chatRoom);
    }
    public List<String> getParticipants(String token, String roomId) throws ChatRoomException, InvalidUserException {
        String userId = jwtService.extractId(token);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Could not find this chat room"));

        if (!chatRoom.getParticipantIds().contains(userId)){
            throw new ChatRoomException("User with id " + userId + " is not a participant of this chat room");
        }
        List<String> participantIds = chatRoom.getParticipantIds();
        System.out.println(participantIds);
        List<String> participantEmails = new ArrayList<>();

        for (String participant : participantIds){
            String participantEmail = getParticipantEmail(participant);
            participantEmails.add(participantEmail);
        }
        return participantEmails;
    }
    private String generateInvitationLink() {
        String invitationToken = UUID.randomUUID().toString();
        return "chatApp/invite/" + invitationToken;
    }
    private boolean isInvitationValid(String invitationLink) throws InvalidInvitationException {
        Invitation invitation = invitationRepository.findByInvitationLink(invitationLink).orElseThrow(() -> new InvalidInvitationException("Invitation link is not valid"));

        Date createdAt = invitation.getCreatedAt();
        Date currentTime = new Date();

        long elapsedTime = currentTime.getTime() - createdAt.getTime();
        long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);

        if (elapsedMinutes > 15){
            invitation.setExpired(true);
            invitationRepository.save(invitation);
            return false;
        } else {
            return true;
        }
    }
    private String getParticipantEmail(String participantId) throws InvalidUserException {
        User user = userRepository.findById(participantId).orElseThrow(() -> new InvalidUserException("Could not find user with this id"));
        return user.getEmail();
    }
}
