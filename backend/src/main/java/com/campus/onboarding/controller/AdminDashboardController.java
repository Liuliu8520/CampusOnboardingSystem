package com.campus.onboarding.controller;

import com.campus.onboarding.common.Result;
import com.campus.onboarding.service.AdminWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final AdminWorkflowService adminWorkflowService;

    public AdminDashboardController(AdminWorkflowService adminWorkflowService) {
        this.adminWorkflowService = adminWorkflowService;
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(adminWorkflowService.dashboard());
    }
}
