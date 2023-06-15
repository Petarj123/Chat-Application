package com.auth.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AuthenticationResponse {
    @JsonProperty("token")
    private String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }
}
