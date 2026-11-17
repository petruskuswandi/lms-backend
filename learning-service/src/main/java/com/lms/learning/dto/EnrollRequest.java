package com.lms.learning.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnrollRequest(
    @NotNull(message = "courseId wajib diisi")
    UUID courseId
) {}