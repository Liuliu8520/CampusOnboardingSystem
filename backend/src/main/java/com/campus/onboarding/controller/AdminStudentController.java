package com.campus.onboarding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.onboarding.common.Result;
import com.campus.onboarding.dto.StudentPaymentStatusRequest;
import com.campus.onboarding.dto.StudentSaveRequest;
import com.campus.onboarding.entity.Student;
import com.campus.onboarding.service.AdminWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {
    private final AdminWorkflowService adminWorkflowService;

    public AdminStudentController(AdminWorkflowService adminWorkflowService) {
        this.adminWorkflowService = adminWorkflowService;
    }

    @GetMapping
    public Result<Page<Student>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String college,
                                      @RequestParam(required = false) String major,
                                      @RequestParam(required = false) Boolean checkedIn) {
        return Result.ok(adminWorkflowService.studentPage(page, size, keyword, college, major, checkedIn));
    }

    @PostMapping
    public Result<Student> create(@Valid @RequestBody StudentSaveRequest request) {
        return Result.ok(adminWorkflowService.saveStudent(request));
    }

    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @Valid @RequestBody StudentSaveRequest request) {
        StudentSaveRequest merged = new StudentSaveRequest(
                id,
                request.studentId(),
                request.name(),
                request.gender(),
                request.college(),
                request.major(),
                request.className(),
                request.phone(),
                request.idCard(),
                request.address(),
                request.paid(),
                request.checkedIn()
        );
        return Result.ok(adminWorkflowService.saveStudent(merged));
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminWorkflowService.resetStudentPassword(id);
        return Result.ok();
    }

    @PutMapping("/{id}/checkin")
    public Result<Student> checkin(@PathVariable Long id) {
        return Result.ok(adminWorkflowService.adminCheckin(id));
    }

    @PutMapping("/{id}/payments")
    public Result<Student> updatePayments(@PathVariable Long id, @RequestBody StudentPaymentStatusRequest request) {
        return Result.ok(adminWorkflowService.updateStudentPayments(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminWorkflowService.deleteStudent(id);
        return Result.ok();
    }
}
