package com.campus.onboarding.dto;

public record LoginResponse(
        String token,
        String role,
        String account,
        String displayName
) {
}
