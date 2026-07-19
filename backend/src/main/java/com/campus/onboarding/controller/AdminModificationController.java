package com.campus.onboarding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.onboarding.common.Result;
import com.campus.onboarding.dto.ModificationReviewRequest;
import com.campus.onboarding.entity.QualificationModification;
import com.campus.onboarding.service.AdminWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/modifications")
public class AdminModificationController {
    private final AdminWorkflowService adminWorkflowService;

    public AdminModificationController(AdminWorkflowService adminWorkflowService) {
        this.adminWorkflowService = adminWorkflowService;
    }

    @GetMapping("/list")
    public Result<Page<QualificationModification>> page(@RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size,
                                                        @RequestParam(required = false) String status) {
        return Result.ok(adminWorkflowService.modificationPage(page, size, status));
    }

    @PutMapping("/approve/{id}")
    public Result<QualificationModification> review(@PathVariable Long id,
                                                    @Valid @RequestBody ModificationReviewRequest request) {
        return Result.ok(adminWorkflowService.reviewModification(id, request));
    }
}
