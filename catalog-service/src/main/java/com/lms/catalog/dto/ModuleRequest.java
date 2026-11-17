package com.lms.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record ModuleRequest(
    @NotBlank(message = "Judul modul wajib diisi")
    String title,
    int position
) {}