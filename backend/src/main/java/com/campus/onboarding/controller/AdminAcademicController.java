package com.campus.onboarding.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.onboarding.common.Result;
import com.campus.onboarding.entity.College;
import com.campus.onboarding.entity.Major;
import com.campus.onboarding.mapper.CollegeMapper;
import com.campus.onboarding.mapper.MajorMapper;
import com.campus.onboarding.security.AuthContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academics")
public class AdminAcademicController {
    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    public AdminAcademicController(CollegeMapper collegeMapper, MajorMapper majorMapper) {
        this.collegeMapper = collegeMapper;
        this.majorMapper = majorMapper;
    }

    @GetMapping("/colleges")
    public Result<List<College>> colleges(@RequestParam(defaultValue = "true") Boolean enabled) {
        AuthContext.requireAdmin();
        QueryWrapper<College> wrapper = new QueryWrapper<College>()
                .orderByAsc("sort_no")
                .orderByAsc("id");
        if (enabled != null) {
            wrapper.eq("is_enabled", enabled);
        }
        return Result.ok(collegeMapper.selectList(wrapper));
    }

    @GetMapping("/majors")
    public Result<List<Major>> majors(@RequestParam(required = false) Long collegeId,
                                      @RequestParam(required = false) String college,
                                      @RequestParam(defaultValue = "true") Boolean enabled) {
        AuthContext.requireAdmin();
        Long targetCollegeId = collegeId;
        if (targetCollegeId == null && StringUtils.hasText(college)) {
            College targetCollege = collegeMapper.selectOne(new QueryWrapper<College>()
                    .eq("name", college)
                    .last("LIMIT 1"));
            if (targetCollege == null) {
                return Result.ok(List.of());
            }
            targetCollegeId = targetCollege.getId();
        }

        QueryWrapper<Major> wrapper = new QueryWrapper<Major>()
                .orderByAsc("college_id")
                .orderByAsc("sort_no")
                .orderByAsc("id");
        if (targetCollegeId != null) {
            wrapper.eq("college_id", targetCollegeId);
        }
        if (enabled != null) {
            wrapper.eq("is_enabled", enabled);
        }
        return Result.ok(majorMapper.selectList(wrapper));
    }
}
