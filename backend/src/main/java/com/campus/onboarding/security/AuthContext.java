package com.campus.onboarding.security;

import com.campus.onboarding.common.BizException;

public final class AuthContext {
    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        AuthUser user = HOLDER.get();
        if (user == null) {
            throw new BizException(401, "登录已失效，请重新登录");
        }
        return user;
    }

    public static AuthUser requireStudent() {
        AuthUser user = get();
        if (!user.isStudent()) {
            throw new BizException(403, "仅学生可操作");
        }
        return user;
    }

    public static AuthUser requireAdmin() {
        AuthUser user = get();
        if (!user.isAdmin()) {
            throw new BizException(403, "仅管理员可操作");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
