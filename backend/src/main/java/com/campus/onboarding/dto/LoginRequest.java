package com.campus.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String role,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String captcha,
        String captchaId
) {
}
