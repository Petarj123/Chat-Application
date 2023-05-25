package com.auth.app.repository;

import com.auth.app.model.ChatRoomTopic;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomTopicRepository extends MongoRepository<ChatRoomTopic, String> {

}
