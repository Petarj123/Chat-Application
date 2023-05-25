package com.auth.app.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "topics")
@Builder
public class ChatRoomTopic {
    @Id
    private String topicId;
    private String chatRoomId;
    private String topic;
}
