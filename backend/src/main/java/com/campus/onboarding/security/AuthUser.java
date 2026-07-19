package com.campus.onboarding.security;

public record AuthUser(String account, String role, String displayName) {
    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
