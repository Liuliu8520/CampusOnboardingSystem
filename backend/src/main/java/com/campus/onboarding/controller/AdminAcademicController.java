package com.campus.onboarding.controller;

import com.campus.onboarding.common.Result;
import com.campus.onboarding.entity.College;
import com.campus.onboarding.entity.Major;
import com.campus.onboarding.entity.SchoolClass;
import com.campus.onboarding.service.AcademicService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academics")
public class AdminAcademicController {
    private final AcademicService academicService;

    public AdminAcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // ===== 学院 =====

    @GetMapping("/colleges")
    public Result<List<College>> colleges(@RequestParam(required = false) Boolean enabled) {
        return Result.ok(academicService.listColleges(enabled));
    }

    @PostMapping("/colleges")
    public Result<College> createCollege(@RequestBody College college) {
        return Result.ok(academicService.saveCollege(college));
    }

    @PutMapping("/colleges/{id}")
    public Result<College> updateCollege(@PathVariable Long id, @RequestBody College college) {
        college.setId(id);
        return Result.ok(academicService.saveCollege(college));
    }

    @DeleteMapping("/colleges/{id}")
    public Result<Void> deleteCollege(@PathVariable Long id) {
        academicService.deleteCollege(id);
        return Result.ok();
    }

    // ===== 专业 =====

    @GetMapping("/majors")
    public Result<List<Major>> majors(@RequestParam(required = false) Long collegeId,
                                      @RequestParam(required = false) String college,
                                      @RequestParam(required = false) Boolean enabled) {
        return Result.ok(academicService.listMajors(collegeId, college, enabled));
    }

    @PostMapping("/majors")
    public Result<Major> createMajor(@RequestBody Major major) {
        return Result.ok(academicService.saveMajor(major));
    }

    @PutMapping("/majors/{id}")
    public Result<Major> updateMajor(@PathVariable Long id, @RequestBody Major major) {
        major.setId(id);
        return Result.ok(academicService.saveMajor(major));
    }

    @DeleteMapping("/majors/{id}")
    public Result<Void> deleteMajor(@PathVariable Long id) {
        academicService.deleteMajor(id);
        return Result.ok();
    }

    // ===== 班级 =====

    @GetMapping("/classes")
    public Result<List<SchoolClass>> classes(@RequestParam(required = false) Long majorId,
                                             @RequestParam(required = false) Boolean enabled) {
        return Result.ok(academicService.listClasses(majorId, enabled));
    }

    @PostMapping("/classes")
    public Result<SchoolClass> createClass(@RequestBody SchoolClass schoolClass) {
        return Result.ok(academicService.saveClass(schoolClass));
    }

    @PutMapping("/classes/{id}")
    public Result<SchoolClass> updateClass(@PathVariable Long id, @RequestBody SchoolClass schoolClass) {
        schoolClass.setId(id);
        return Result.ok(academicService.saveClass(schoolClass));
    }

    @DeleteMapping("/classes/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        academicService.deleteClass(id);
        return Result.ok();
    }
}
