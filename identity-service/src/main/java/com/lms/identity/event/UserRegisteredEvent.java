package com.lms.identity.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRegisteredEvent(
    UUID userId,
    String email,
    String fullName,
    String role,
    OffsetDateTime registeredAt
) {}