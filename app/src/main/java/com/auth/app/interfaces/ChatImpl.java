package com.auth.app.interfaces;

import com.auth.app.DTO.InvitationRequest;

public interface ChatImpl {

    void createChatRoom(String token);
    void sendInvitation(InvitationRequest invitationRequest, String token);
    void acceptInvitation(String invitationId, String token);
    void declineInvitation(String invitationId, String token);
}
