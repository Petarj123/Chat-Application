package com.auth.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "invites")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invitation {

    @Id
    private String id;
    private String senderId;
    private String chatroomId;
    private String invitationLink;
    private Date createdAt;
    private boolean isExpired;

}
