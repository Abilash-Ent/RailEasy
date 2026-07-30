package com.apexon.railEasy.dto.response;

import com.apexon.railEasy.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response containing the issued JWT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long expiresInSeconds;
    private Long userId;
    private String email;
    private Role role;
}

