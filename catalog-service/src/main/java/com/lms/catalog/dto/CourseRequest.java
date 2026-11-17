package com.lms.catalog.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CourseRequest(
    @NotBlank(message = "Judul kursus wajib diisi")
    String title,
    String description,
    @PositiveOrZero(message = "Harga tidak boleh negatif")
    BigDecimal price,
    String status
) {}