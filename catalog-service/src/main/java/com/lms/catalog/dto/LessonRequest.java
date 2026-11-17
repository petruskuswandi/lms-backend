package com.lms.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record LessonRequest(
    @NotBlank(message = "Judul materi wajib diisi")
    String title,
    String contentType,
    String contentUrl,
    int durationSeconds,
    int position
) {}