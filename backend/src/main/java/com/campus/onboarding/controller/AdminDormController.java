package com.campus.onboarding.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.onboarding.common.BizException;
import com.campus.onboarding.common.Result;
import com.campus.onboarding.dto.DormBatchCreateRequest;
import com.campus.onboarding.entity.DormBuilding;
import com.campus.onboarding.entity.DormRoom;
import com.campus.onboarding.mapper.DormBuildingMapper;
import com.campus.onboarding.mapper.DormRoomMapper;
import com.campus.onboarding.security.AuthContext;
import com.campus.onboarding.service.AdminWorkflowService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dorm")
public class AdminDormController {
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final AdminWorkflowService adminWorkflowService;

    public AdminDormController(DormBuildingMapper buildingMapper,
                               DormRoomMapper roomMapper,
                               AdminWorkflowService adminWorkflowService) {
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
        this.adminWorkflowService = adminWorkflowService;
    }

    @GetMapping("/buildings")
    public Result<List<DormBuilding>> buildings() {
        AuthContext.requireAdmin();
        return Result.ok(buildingMapper.selectList(new QueryWrapper<DormBuilding>().orderByAsc("sort_no").orderByAsc("id")));
    }

    @PostMapping("/buildings")
    public Result<DormBuilding> createBuilding(@RequestBody DormBuilding building) {
        AuthContext.requireAdmin();
        buildingMapper.insert(building);
        return Result.ok(building);
    }

    @PutMapping("/buildings/{id}")
    public Result<DormBuilding> updateBuilding(@PathVariable Long id, @RequestBody DormBuilding building) {
        AuthContext.requireAdmin();
        building.setId(id);
        buildingMapper.updateById(building);
        return Result.ok(buildingMapper.selectById(id));
    }

    @DeleteMapping("/buildings/{id}")
    public Result<Void> deleteBuilding(@PathVariable Long id) {
        AuthContext.requireAdmin();
        long rooms = roomMapper.selectCount(new QueryWrapper<DormRoom>().eq("building_id", id));
        if (rooms > 0) {
            throw new BizException("楼栋下已有房间，不能删除");
        }
        buildingMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/rooms")
    public Result<List<DormRoom>> rooms(@RequestParam(required = false) Long buildingId,
                                        @RequestParam(required = false) String major,
                                        @RequestParam(required = false) String gender) {
        AuthContext.requireAdmin();
        QueryWrapper<DormRoom> wrapper = new QueryWrapper<DormRoom>().orderByAsc("building_id").orderByAsc("room_no");
        if (buildingId != null) {
            wrapper.eq("building_id", buildingId);
        }
        if (StringUtils.hasText(major)) {
            wrapper.like("major", major);
        }
        if (StringUtils.hasText(gender)) {
            wrapper.eq("gender", gender);
        }
        return Result.ok(roomMapper.selectList(wrapper));
    }

    @PostMapping("/rooms/batch")
    public Result<List<DormRoom>> batchCreateRooms(@Valid @RequestBody DormBatchCreateRequest request) {
        return Result.ok(adminWorkflowService.batchCreateRooms(request));
    }

    @DeleteMapping("/rooms/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        adminWorkflowService.deleteRoom(id);
        return Result.ok();
    }

    @GetMapping("/occupancy")
    public Result<List<Map<String, Object>>> occupancy() {
        return Result.ok(adminWorkflowService.occupancy());
    }
}
