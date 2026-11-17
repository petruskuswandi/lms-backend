package com.lms.catalog.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CatalogEvent(
    String eventType, // COURSE_CREATED, COURSE_PUBLISHED, COURSE_UPDATED, COURSE DELETED, COURSE_CONTENT_UPDATED
    UUID courseId,
    String title,
    UUID instructorId,
    BigDecimal price,
    String status,
    int totalLessons,
    OffsetDateTime occurredAt
) {}