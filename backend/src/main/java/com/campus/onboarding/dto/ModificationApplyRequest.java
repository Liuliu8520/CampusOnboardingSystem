package com.campus.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public record ModificationApplyRequest(
        @NotBlank String fieldName,
        @NotBlank String newValue,
        @NotBlank String reason
) {
}
