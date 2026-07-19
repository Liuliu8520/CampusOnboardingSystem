package com.campus.onboarding.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PaymentRequest(
        @NotEmpty List<Long> feeItemIds
) {
}
