package com.campus.onboarding.controller;

import com.campus.onboarding.common.Result;
import com.campus.onboarding.dto.ModificationApplyRequest;
import com.campus.onboarding.dto.PaymentRequest;
import com.campus.onboarding.dto.StudentProfileResponse;
import com.campus.onboarding.entity.Announcement;
import com.campus.onboarding.entity.QualificationModification;
import com.campus.onboarding.entity.Student;
import com.campus.onboarding.service.StudentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentWorkflowService studentWorkflowService;

    public StudentController(StudentWorkflowService studentWorkflowService) {
        this.studentWorkflowService = studentWorkflowService;
    }

    @GetMapping("/qualification")
    public Result<StudentProfileResponse> qualification() {
        return Result.ok(studentWorkflowService.profile());
    }

    @GetMapping("/profile")
    public Result<StudentProfileResponse> profile() {
        return Result.ok(studentWorkflowService.profile());
    }

    @PostMapping("/qualification/apply")
    public Result<QualificationModification> apply(@Valid @RequestBody ModificationApplyRequest request) {
        return Result.ok(studentWorkflowService.applyModification(request));
    }

    @PostMapping("/qualification/confirm")
    public Result<Student> confirm() {
        return Result.ok(studentWorkflowService.confirmQualification());
    }

    @GetMapping("/payment/items")
    public Result<List<Map<String, Object>>> paymentItems() {
        return Result.ok(studentWorkflowService.paymentItems());
    }

    @GetMapping("/payment/status")
    public Result<List<Map<String, Object>>> paymentStatus() {
        return Result.ok(studentWorkflowService.paymentItems());
    }

    @PostMapping("/payment")
    public Result<Map<String, Object>> payment(@Valid @RequestBody PaymentRequest request) {
        return Result.ok(studentWorkflowService.pay(request));
    }

    @GetMapping("/dorm")
    public Result<Map<String, Object>> dorm() {
        return Result.ok(studentWorkflowService.dormDetail(studentWorkflowService.currentStudent()));
    }

    @PostMapping("/dorm/assign")
    public Result<Map<String, Object>> assignDorm() {
        return Result.ok(studentWorkflowService.assignDorm());
    }

    @PostMapping("/checkin")
    public Result<StudentProfileResponse> checkin() {
        return Result.ok(studentWorkflowService.checkin());
    }

    @GetMapping("/announcements")
    public Result<List<Announcement>> announcements() {
        return Result.ok(studentWorkflowService.publishedAnnouncements());
    }

    @GetMapping("/announcements/{id}")
    public Result<Announcement> announcement(@PathVariable Long id) {
        return Result.ok(studentWorkflowService.publishedAnnouncement(id));
    }
}
