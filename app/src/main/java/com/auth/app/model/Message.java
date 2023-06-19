package com.auth.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
@Document(collection = "messages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    private String messageId;
    private String text;
    private String sender;
    private Date sentAt;
    private Binary voiceMessage;
}
