package com.campus.onboarding.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.onboarding.common.Result;
import com.campus.onboarding.entity.FeeItem;
import com.campus.onboarding.mapper.FeeItemMapper;
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
@RequestMapping("/api/admin/fees")
public class AdminFeeController {
    private final FeeItemMapper feeItemMapper;

    public AdminFeeController(FeeItemMapper feeItemMapper) {
        this.feeItemMapper = feeItemMapper;
    }

    @GetMapping
    public Result<Page<FeeItem>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size) {
        AuthContext.requireAdmin();
        return Result.ok(feeItemMapper.selectPage(Page.of(page, size), new QueryWrapper<FeeItem>().orderByAsc("id")));
    }

    @PostMapping
    public Result<FeeItem> create(@RequestBody FeeItem feeItem) {
        AuthContext.requireAdmin();
        feeItemMapper.insert(feeItem);
        return Result.ok(feeItem);
    }

    @PutMapping("/{id}")
    public Result<FeeItem> update(@PathVariable Long id, @RequestBody FeeItem feeItem) {
        AuthContext.requireAdmin();
        feeItem.setId(id);
        feeItemMapper.updateById(feeItem);
        return Result.ok(feeItemMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AuthContext.requireAdmin();
        feeItemMapper.deleteById(id);
        return Result.ok();
    }
}
