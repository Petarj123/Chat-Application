package com.auth.app.service;

import com.auth.app.jwt.JwtService;
import com.auth.app.model.ChatRoom;
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
            if (chatRoomRepository.findById(roomId).isPresent()){
                ChatRoom existingChatRoom = chatRoomRepository.findById(roomId).orElseThrow();
                chatRooms.add(existingChatRoom);
            }
        }
        return chatRooms;
    }
}
