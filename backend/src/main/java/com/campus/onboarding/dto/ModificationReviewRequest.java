package com.campus.onboarding.dto;

import jakarta.validation.constraints.NotNull;

public record ModificationReviewRequest(
        @NotNull Boolean approved,
        String comment
) {
}
