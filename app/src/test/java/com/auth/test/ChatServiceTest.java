package com.auth.test;

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
import com.auth.app.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    @InjectMocks
    private ChatService chatService;
    @Mock
    private JwtService jwtService;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Test
    public void createChatRoom_success() {
        String token = "testToken";
        String roomName = "testRoom";
        String userId = "testUserId";
        User user = new User();
        user.setChatRooms(new ArrayList<>());
        when(jwtService.extractId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        chatService.createChatRoom(token, roomName);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    public void createChatRoom_userNotFound() {
        String token = "testToken";
        String roomName = "testRoom";
        String userId = "testUserId";
        when(jwtService.extractId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> chatService.createChatRoom(token, roomName));
    }
    // ... Add more test cases for createChatRoom ...
    @Test
    public void createInvite_success() {
        String token = "testToken";
        String roomId = "testRoomId";
        String userId = "testUserId";
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setParticipantIds(List.of(userId));
        when(jwtService.extractId(token)).thenReturn(userId);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        String invitationLink = chatService.createInvite(token, roomId);
        assertTrue(invitationLink.startsWith("chatApp/invite/"));
        verify(invitationRepository, times(1)).save(any(Invitation.class));
    }
    @Test
    public void createInvite_userNotInChatRoom() {
        String token = "testToken";
        String roomId = "testRoomId";
        String userId = "testUserId";
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setParticipantIds(new ArrayList<>());
        when(jwtService.extractId(token)).thenReturn(userId);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        assertThrows(RuntimeException.class, () -> chatService.createInvite(token, roomId));
    }
    // ... Add more test cases for createInvite ...
    @Test
    public void acceptInvite_success() throws InvalidInvitationException, ChatRoomException {
        String token = "testToken";
        String invitationLink = "chatApp/invite/testInvitationToken";
        String userId = "testUserId";
        String roomId = "testRoomId";
        User user = new User();
        user.setChatRooms(new ArrayList<>());
        Invitation invitation = new Invitation();
        invitation.setChatroomId(roomId);
        invitation.setCreatedAt(new Date());
        invitation.setExpired(false);
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setParticipantIds(new ArrayList<>());
        when(jwtService.extractId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(invitationRepository.findByInvitationLink(invitationLink)).thenReturn(Optional.of(invitation));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        chatService.acceptInvite(token, invitationLink);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(invitationRepository, times(1)).save(any(Invitation.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
    // ... Add more test cases for acceptInvite ...
    @Test
    public void sendMessage_success() {
        String roomId = "testRoomId";
        String text = "testMessage";
        String token = "testToken";
        String userId = "testUserId";
        String username = "testUsername";
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setParticipantIds(List.of(userId));
        chatRoom.setMessages(new ArrayList<>());
        when(jwtService.extractId(token)).thenReturn(userId);
        when(jwtService.extractEmail(token)).thenReturn(username);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        Message message = chatService.sendMessage(roomId, text, token);
        assertEquals(username, message.getSender());
        assertEquals(text, message.getText());
        assertNotNull(message.getSentAt());
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }
    // ... Add more test cases for sendMessage ...
    @Test
    public void getParticipants_success() throws ChatRoomException, InvalidUserException {
        String token = "testToken";
        String roomId = "testRoomId";
        String userId = "testUserId";
        String userEmail = "testUserEmail";
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setParticipantIds(List.of(userId));
        User user = new User();
        user.setEmail(userEmail);
        when(jwtService.extractId(token)).thenReturn(userId);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        List<String> participants = chatService.getParticipants(token, roomId);
        assertEquals(1, participants.size());
        assertEquals(userEmail, participants.get(0));
    }
}