package com.auth.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "invites")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invitation {

    @Id
    private String id;
    private String senderId;
    private String recipientId;
    private InvitationStatus status;

}
