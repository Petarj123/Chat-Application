package com.auth.app.service;

import com.auth.app.exceptions.ChatRoomException;
import com.auth.app.jwt.JwtService;
import com.auth.app.model.ChatRoom;
import com.auth.app.model.Message;
import com.auth.app.model.User;
import com.auth.app.repository.ChatRoomRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoom> getAllChatRooms(String token) {
        String id = jwtService.extractId(token);
        User user = userRepository.findById(id).orElseThrow();

        List<String> roomIds = user.getChatRooms();
        List<ChatRoom> chatRooms = new ArrayList<>();
        for (String roomId : roomIds){
            if (roomId != null){
                if (chatRoomRepository.findById(roomId).isPresent()){
                    ChatRoom existingChatRoom = chatRoomRepository.findById(roomId).orElseThrow();
                    chatRooms.add(existingChatRoom);
                }
            }
        }
        return chatRooms;
    }

    public List<Message> getAllMessages(String token, String roomId) throws ChatRoomException {
        String userId = jwtService.extractId(token);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new ChatRoomException("Chat room does not exist"));

        if (!chatRoom.getParticipantIds().contains(userId)){
            throw new ChatRoomException("User is not a part of this chat room.");
        }

        return chatRoom.getMessages();
    }
}
