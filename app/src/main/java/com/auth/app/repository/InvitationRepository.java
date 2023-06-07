package com.auth.app.repository;

import com.auth.app.model.Invitation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InvitationRepository extends MongoRepository<Invitation, String> {
    Optional<Invitation> findByInvitationLink(String invitationLink);
}
