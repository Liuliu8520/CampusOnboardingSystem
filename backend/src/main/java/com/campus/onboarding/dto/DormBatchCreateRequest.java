package com.campus.onboarding.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DormBatchCreateRequest(
        @NotNull Long buildingId,
        @NotBlank String major,
        @NotBlank String gender,
        @NotNull @Min(1) Integer startNo,
        @NotNull @Min(1) Integer count,
        @NotNull @Min(1) Integer capacity
) {
}
