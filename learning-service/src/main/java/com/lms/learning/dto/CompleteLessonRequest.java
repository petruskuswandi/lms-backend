package com.lms.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CompleteLessonRequest(
    @NotNull(message = "lessonId wajib diisi")
    UUID lessonId,
    @Min(value = 1, message = "totalLessons minimal 1")
    int totalLessons
) {}