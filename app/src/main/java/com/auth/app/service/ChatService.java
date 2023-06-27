package com.auth.app.service;

import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.exceptions.InvalidInvitationException;
import com.auth.app.exceptions.InvalidUserException;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Invitation;
import com.auth.app.model.Message;
import com.auth.app.model.User;
import com.auth.app.repository.ChatRoomRepository;
import com.auth.app.repository.InvitationRepository;
import com.auth.app.repository.MessageRepository;
import com.auth.app.repository.UserRepository;
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
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public void createChatRoom(String token, String roomName) {
        String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow();
        List<String> participants = new ArrayList<>();
        participants.add(userId);
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .participantIds(participants)
                .messages(new ArrayList<>())
                .createdAt(new Date())
                .build();
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
        Invitation invitation = invitationRepository.findByInvitationLink(invitationLink)
                .orElseThrow(() -> new InvalidInvitationException("Invitation link is not valid"));
        if (!isInvitationValid(invitation)){
            throw new InvalidInvitationException("Invitation link is expired");
        }
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

    public Message sendMessage(String roomId, String text, String token){
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

        return message;
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
    private boolean isInvitationValid(Invitation invitation) {
        Date createdAt = invitation.getCreatedAt();
        Date currentTime = new Date();
        long elapsedTime = currentTime.getTime() - createdAt.getTime();
        long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        if (elapsedMinutes > 15){
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
