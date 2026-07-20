package com.campus.onboarding.dto;

import java.util.List;

public record StudentPaymentStatusRequest(
        List<Long> paidFeeItemIds
) {
}
