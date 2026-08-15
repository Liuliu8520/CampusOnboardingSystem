package com.campus.onboarding.controller;

import com.campus.onboarding.common.Result;
import com.campus.onboarding.dto.ChangePasswordRequest;
import com.campus.onboarding.dto.LoginRequest;
import com.campus.onboarding.dto.LoginResponse;
import com.campus.onboarding.service.AuthService;
import com.campus.onboarding.service.CaptchaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        // 返回数学公式题目与 captchaId，答案由服务端暂存，不回传前端
        return Result.ok(captchaService.generate());
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.ok();
    }
}
