package com.lms.identity.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserProfile user
) {
    public record UserProfile(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        String role
    ) {}
}