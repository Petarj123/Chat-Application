package com.auth.app.DTO;

import lombok.Builder;
import lombok.Data;

/**
 * The type Authentication response.
 */
@Data
@Builder
public class AuthenticationResponse {
    private String token;
}
