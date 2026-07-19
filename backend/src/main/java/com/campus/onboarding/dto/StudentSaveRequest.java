package com.campus.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public record StudentSaveRequest(
        Long id,
        @NotBlank String studentId,
        @NotBlank String name,
        @NotBlank String gender,
        @NotBlank String college,
        @NotBlank String major,
        @NotBlank String className,
        String phone,
        String idCard,
        String address,
        Boolean paid,
        Boolean checkedIn
) {
}
