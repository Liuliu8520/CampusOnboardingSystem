package com.campus.onboarding.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.onboarding.common.Result;
import com.campus.onboarding.entity.Announcement;
import com.campus.onboarding.mapper.AnnouncementMapper;
import com.campus.onboarding.security.AuthContext;
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
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {
    private final AnnouncementMapper announcementMapper;

    public AdminAnnouncementController(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @GetMapping
    public Result<Page<Announcement>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size) {
        AuthContext.requireAdmin();
        return Result.ok(announcementMapper.selectPage(Page.of(page, size), new QueryWrapper<Announcement>().orderByDesc("create_time")));
    }

    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        AuthContext.requireAdmin();
        announcementMapper.insert(announcement);
        return Result.ok(announcement);
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        AuthContext.requireAdmin();
        announcement.setId(id);
        announcementMapper.updateById(announcement);
        return Result.ok(announcementMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AuthContext.requireAdmin();
        announcementMapper.deleteById(id);
        return Result.ok();
    }
}
