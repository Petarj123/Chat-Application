package com.auth.app.repository;

import com.auth.app.model.ChatRoomTopic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatRoomTopicRepository extends MongoRepository<ChatRoomTopic, String> {

    Optional<ChatRoomTopic> findByChatRoomId();
}
