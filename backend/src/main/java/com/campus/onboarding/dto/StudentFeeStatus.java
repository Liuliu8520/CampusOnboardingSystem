package com.campus.onboarding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudentFeeStatus(
        Long feeItemId,
        String name,
        BigDecimal amount,
        Boolean required,
        Boolean enabled,
        Boolean paid,
        String status,
        LocalDateTime payTime
) {
}
