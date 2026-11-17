package com.lms.learning.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LearningEvent(
    String eventType, // STUDENT_ENROLLED, COURSE_COMPLETED
    UUID enrollmentId,
    UUID studentId,
    String studentName,
    UUID courseId,
    String courseTitle,
    int progressPercentage,
    OffsetDateTime occurredAt
) {}