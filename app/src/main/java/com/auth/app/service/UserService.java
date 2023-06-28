package com.auth.app.service;

import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.exceptions.InvalidEmailException;
import com.auth.app.exceptions.InvalidPasswordException;
import com.auth.app.exceptions.InvalidUserException;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Message;
import com.auth.app.model.User;
import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.ChatRoomRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    public List<ChatRoom> getAllChatRooms(String token) {
        String id = jwtService.extractId(token);
        User user = userRepository.findById(id).orElseThrow();

        List<String> roomIds = user.getChatRooms();
        List<ChatRoom> chatRooms = new ArrayList<>(roomIds.size());
        for (String roomId : roomIds) {
            if (roomId != null) {
                chatRoomRepository.findById(roomId).ifPresent(chatRooms::add);
            }
        }
        return chatRooms;
    }

    public List<Message> getAllMessages(String token, String roomId) throws ChatRoomException {
        String userId = jwtService.extractId(token);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomException("Chat room does not exist"));
        if (!chatRoom.getParticipantIds().contains(userId)) {
            throw new ChatRoomException("User is not a part of this chat room.");
        }
        return chatRoom.getMessages();
    }
    public void changePassword(String token, String oldPassword, String newPassword) throws InvalidPasswordException, InvalidUserException {
        String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidUserException("User does not exist"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    public void changeEmail(String token, String newEmail) throws InvalidEmailException {
        String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidEmailException("Invalid email"));
        if (userRepository.existsByEmail(newEmail) || adminRepository.existsByEmail(newEmail)) {
            throw new InvalidEmailException("Email is already in use");
        } else if (user.getEmail().equals(newEmail)) {
            throw new InvalidEmailException(newEmail + " is already your email address");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
    }
    public void leaveChatRoom(String token, String roomId) throws ChatRoomException, InvalidUserException {
        String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidUserException("User does not exist"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
              .orElseThrow(() -> new ChatRoomException("Chat room does not exist"));
        if (!chatRoom.getParticipantIds().contains(userId)) {
            throw new ChatRoomException("User is not a part of this chat room.");
        }
        removeChatRoomFromUser(user, chatRoom.getId());
        removeParticipantsFromChatRoom(chatRoom, user.getUserId());
    }


    // PRIVATE METHODS
    private void removeParticipantsFromChatRoom(ChatRoom chatRoom, String userId){
        List<String> participants = chatRoom.getParticipantIds();
        participants.remove(userId);
        chatRoom.setParticipantIds(participants);
        chatRoomRepository.save(chatRoom);
    }
    private void removeChatRoomFromUser(User user, String roomId){
        List<String> chatRooms = user.getChatRooms();
        chatRooms.remove(roomId);
        user.setChatRooms(chatRooms);
        userRepository.save(user);
    }
}
