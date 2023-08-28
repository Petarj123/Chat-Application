package com.auth.app.service;

import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.exceptions.InvalidInvitationException;
import com.auth.app.exceptions.InvalidUserException;
import com.auth.app.jwt.service.JwtService;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Invitation;
import com.auth.app.model.Message;
import com.auth.app.model.user.model.User;
import com.auth.app.model.user.repository.UserRepository;
import com.auth.app.model.user.service.UserService;
import com.auth.app.repository.ChatRoomRepository;
import com.auth.app.repository.InvitationRepository;
import com.auth.app.repository.MessageRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;
    private final InvitationRepository invitationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public void createChatRoom(String token, String roomName) {
        String userId = jwtService.getId(token);
        User user = userRepository.findById(userId).orElseThrow();
        Set<String> participants = new HashSet<>();
        participants.add(userId);
        Set<String> admins = new HashSet<>();
        admins.add(userId);
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .participantIds(participants)
                .groupAdmins(admins)
                .messages(new ArrayList<>())
                .createdAt(new Date())
                .createdBy(userId)
                .build();
        chatRoomRepository.save(chatRoom);
        Set<String> userChatRooms = user.getChatRooms();
        userChatRooms.add(chatRoom.getId());
        user.setChatRooms(userChatRooms);
        userRepository.save(user);
    }

    public String createInvite(String token, String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        String userId = jwtService.getId(token);

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
        String userId = jwtService.getId(token);
        User user = userRepository.findById(userId).orElseThrow();
        Invitation invitation = invitationRepository.findByInvitationLink(invitationLink)
                .orElseThrow(() -> new InvalidInvitationException("Invitation link is not valid"));
        if (!isInvitationValid(invitation)){
            throw new InvalidInvitationException("Invitation link is expired");
        }
        ChatRoom chatRoom = chatRoomRepository.findById(invitation.getChatroomId()).orElseThrow();
        Set<String> chatRoomParticipants = chatRoom.getParticipantIds();
        Set<String> userChatRooms = user.getChatRooms();
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
    public List<ChatRoom> leaveChatRoom(String token, String roomId) throws ChatRoomException, InvalidUserException {
        String userId = jwtService.getId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidUserException("User does not exist"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomException("Chat room does not exist"));
        if (!chatRoom.getParticipantIds().contains(userId)) {
            throw new ChatRoomException("User is not a part of this chat room.");
        }
        removeChatRoomFromUser(user, chatRoom.getId());
        removeParticipantsFromChatRoom(chatRoom, user.getId());

        return userService.getAllChatRooms(token);
    }
    public Message sendMessage(String roomId, String text, String token){
        String userId = jwtService.getId(token);
        String username = jwtService.getEmail(token);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        Set<String> participants = chatRoom.getParticipantIds();
        List<Message> messages = chatRoom.getMessages();

        if (!participants.contains(userId)){
            throw new JwtException("User " + userId + "is not a participant of this group");
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
    public Map<String, String> getParticipants(String token, String roomId) throws ChatRoomException, InvalidUserException {
        String userId = jwtService.getId(token);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Could not find this chat room"));

        if (!chatRoom.getParticipantIds().contains(userId)){
            throw new ChatRoomException("User with id " + userId + " is not a participant of this chat room");
        }
        Set<String> participantIds = chatRoom.getParticipantIds();
        System.out.println(participantIds);
        Map<String, String> participantEmails = new HashMap<>();

        for (String participant : participantIds){
            User user = getParticipant(participant);
            if (isGroupCreator(chatRoom, user.getId())){
                participantEmails.put(user.getEmail(), "GROUP CREATOR");
            } else if (isGroupAdmin(chatRoom, user.getId())) {
                participantEmails.put(user.getEmail(), "GROUP ADMIN");
            } else {
                participantEmails.put(user.getEmail(), "PARTICIPANT");
            }
        }
        return participantEmails;
    }
    public Map<String, String> promoteToGroupAdmin(String token, String roomId, String userEmail) throws InvalidUserException, ChatRoomException {
        String groupAdminId = jwtService.getId(token);
        User groupAdmin = userRepository.findById(groupAdminId).orElseThrow(() -> new InvalidUserException("User with id " + groupAdminId + " is not admin of this chat room"));
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new InvalidUserException("User " + userEmail + " is not a part of this chat room"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Invalid chat room"));
        if (isGroupAdmin(chatRoom, groupAdmin.getId())){
            promoteParticipantToGroupAdmin(chatRoom, user);
            return getParticipants(token, roomId);
        } else throw new ChatRoomException("Only group admins can grant admin role to group participants");

    }
    public Map<String, String> demoteGroupAdmin(String token, String roomId, String adminEmail) throws ChatRoomException, InvalidUserException {
        String creatorId = jwtService.getId(token);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Invalid chat room"));
        User groupAdmin = userRepository.findByEmail(adminEmail).orElseThrow(() -> new InvalidUserException("User does not exist"));
        if (!isGroupCreator(chatRoom, creatorId)){
            throw new ChatRoomException("Only group creators can demote admins");
        }
        demoteGroupAdmin(chatRoom, groupAdmin.getId());

        return getParticipants(token, roomId);
    }
    public Map<String, Object> kickUserFromGroup(String token, String roomId, String userEmail) throws ChatRoomException, InvalidUserException {
        String groupAdminId = jwtService.getId(token);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new InvalidUserException("User does not exist"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Invalid chat room"));
        if (!isGroupAdmin(chatRoom, groupAdminId)){
            throw new ChatRoomException("Only group admins can kick participants from group");
        }
        removeUserFromGroup(chatRoom, groupAdminId, user.getId());
        Message message = sendMessage(roomId, userEmail + " has been kicked by " + jwtService.getEmail(token), token);
        Map<String, String> participantsMap = getParticipants(token, roomId);
        Map<String, Object> combinedMap = new HashMap<>(participantsMap);
        combinedMap.put("message", message);
        return combinedMap;
    }

    public Map<String, String> getGroupRole(String token, String roomId) throws ChatRoomException {
        String userId = jwtService.getId(token);
        String userEmail = jwtService.getEmail(token);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Invalid chat room"));

        Map<String, String> response = new HashMap<>();
        if (!chatRoom.getParticipantIds().contains(userId)){
            throw new ChatRoomException("User with id " + userId + " is not a participant of this chat room");
        }

        if (isGroupCreator(chatRoom, userId)){
            response.put(userEmail, "GROUP CREATOR");
            return response;
        } else if (isGroupAdmin(chatRoom, userId)){
            response.put(userEmail, "GROUP ADMIN");
            return response;
        } else {
            response.put(userEmail, "PARTICIPANT");
            return response;
        }
    }

    // PRIVATE METHODS
    private String generateInvitationLink() {
        String invitationToken = UUID.randomUUID().toString();
        return "chatApp/invite/" + invitationToken;
    }
    private boolean isInvitationValid(Invitation invitation) throws InvalidInvitationException {
        if (invitation.isExpired()){
            throw new InvalidInvitationException("Invitation has expired");
        }
        Date createdAt = invitation.getCreatedAt();
        Date currentTime = new Date();
        long elapsedTime = currentTime.getTime() - createdAt.getTime();
        long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        return elapsedMinutes <= 15;
    }
    private User getParticipant(String participantId) throws InvalidUserException {
        return userRepository.findById(participantId).orElseThrow(() -> new InvalidUserException("Could not find user with this id"));
    }
    private void promoteParticipantToGroupAdmin(ChatRoom chatRoom, User user){
        Set<String> chatRoomAdmins = chatRoom.getGroupAdmins();
        chatRoomAdmins.add(user.getId());
        chatRoom.setGroupAdmins(chatRoomAdmins);
        chatRoomRepository.save(chatRoom);
    }
    private void demoteGroupAdmin(ChatRoom chatRoom, String adminId) throws ChatRoomException {
        if (!isGroupAdmin(chatRoom, adminId)){
            throw new ChatRoomException("User is not a group admin!");
        }
        Set<String> groupAdmins = chatRoom.getGroupAdmins();
        groupAdmins.remove(adminId);
        chatRoom.setGroupAdmins(groupAdmins);
        chatRoomRepository.save(chatRoom);
    }
    private boolean isGroupAdmin(ChatRoom chatRoom, String userId){
        return chatRoom.getGroupAdmins().contains(userId);
    }
    private boolean isGroupCreator(ChatRoom chatRoom, String userId){
        return chatRoom.getCreatedBy().equals(userId);
    }
    private void removeUserFromGroup(ChatRoom chatRoom, String groupAdminId, String userId) throws ChatRoomException, InvalidUserException {
        boolean isAdmin = isGroupAdmin(chatRoom, groupAdminId);
        boolean isUserAdmin = isGroupAdmin(chatRoom, userId);
        boolean isCreator = isGroupCreator(chatRoom, groupAdminId);

        if (isAdmin && isUserAdmin && !isCreator) {
            throw new ChatRoomException("Only room creator can kick admins");
        }

        if (isCreator && isUserAdmin || isAdmin) {
            User user = userRepository.findById(userId).orElseThrow(() -> new InvalidUserException("Could not find user with this id"));
            Set<String> participants = chatRoom.getParticipantIds();

            if (!participants.contains(userId)){
                throw new ChatRoomException("User is not a part of this chat room!");
            }

            participants.remove(userId);
            chatRoom.setParticipantIds(participants);

            Set<String> userChatRooms = user.getChatRooms();
            if (!userChatRooms.contains(chatRoom.getId())){
                throw new ChatRoomException("User is not a part of this chat room!");
            }

            userChatRooms.remove(chatRoom.getId());
            user.setChatRooms(userChatRooms);

            chatRoomRepository.save(chatRoom);
            userRepository.save(user);
        } else {
            throw new ChatRoomException("Only room creator or an admin can kick users");
        }
    }
    private void removeParticipantsFromChatRoom(ChatRoom chatRoom, String userId){
        Set<String> participants = chatRoom.getParticipantIds();
        Set<String> admins = chatRoom.getGroupAdmins();

        admins.remove(userId);
        participants.remove(userId);
        chatRoom.setParticipantIds(participants);
        chatRoomRepository.save(chatRoom);
    }
    private void removeChatRoomFromUser(User user, String roomId){
        Set<String> chatRooms = user.getChatRooms();
        chatRooms.remove(roomId);
        user.setChatRooms(chatRooms);
        userRepository.save(user);
    }
}
