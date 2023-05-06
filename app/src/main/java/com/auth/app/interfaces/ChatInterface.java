package com.auth.app.interfaces;

import com.auth.app.DTO.ChatRoomRequest;
import com.auth.app.DTO.InvitationRequest;
import com.auth.app.model.Invitation;

public interface ChatInterface {

    void createChatRoom(ChatRoomRequest room, String token);
    void sendInvitation(InvitationRequest invitationRequest, String token);
    void acceptInvitation(InvitationRequest invitationRequest, String token);
    void declineInvitation(InvitationRequest invitationRequest, String token);
}
