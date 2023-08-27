package com.auth.app.model.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private Set<Role> roles;
    private Set<String> chatRooms;
    private String refreshToken;
    private String resetPasswordToken;
    private Date createdAt;
    private boolean isLocked;
}
