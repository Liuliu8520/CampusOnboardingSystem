package com.campus.onboarding.controller;

import com.campus.onboarding.common.Result;
import com.campus.onboarding.dto.LoginRequest;
import com.campus.onboarding.dto.LoginResponse;
import com.campus.onboarding.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        return Result.ok(Map.of("question", "演示验证码：请输入 6666", "answer", "6666"));
    }
}
