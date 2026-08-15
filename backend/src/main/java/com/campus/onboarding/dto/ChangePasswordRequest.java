package com.campus.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 6, max = 50, message = "新密码长度需在 6-50 之间") String newPassword
) {
}
