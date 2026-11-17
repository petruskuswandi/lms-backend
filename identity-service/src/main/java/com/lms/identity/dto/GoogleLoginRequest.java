package com.lms.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "Google ID Token wajib diisi")
    String idToken
) {}