package com.campus.onboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.onboarding.common.BizException;
import com.campus.onboarding.entity.College;
import com.campus.onboarding.entity.Major;
import com.campus.onboarding.entity.SchoolClass;
import com.campus.onboarding.mapper.ClassMapper;
import com.campus.onboarding.mapper.CollegeMapper;
import com.campus.onboarding.mapper.MajorMapper;
import com.campus.onboarding.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 学院 / 专业 / 班级 三级字典的 CRUD 服务，含唯一性与引用约束校验。
 */
@Service
public class AcademicService {
    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;

    public AcademicService(CollegeMapper collegeMapper, MajorMapper majorMapper, ClassMapper classMapper) {
        this.collegeMapper = collegeMapper;
        this.majorMapper = majorMapper;
        this.classMapper = classMapper;
    }

    // ===== 学院 =====

    public List<College> listColleges(Boolean enabled) {
        AuthContext.requireAdmin();
        QueryWrapper<College> wrapper = new QueryWrapper<College>()
                .orderByAsc("sort_no")
                .orderByAsc("id");
        if (enabled != null) {
            wrapper.eq("is_enabled", enabled);
        }
        return collegeMapper.selectList(wrapper);
    }

    public College saveCollege(College college) {
        AuthContext.requireAdmin();
        if (college.getName() == null || college.getName().trim().isEmpty()) {
            throw new BizException("学院名称不能为空");
        }
        QueryWrapper<College> dup = new QueryWrapper<College>().eq("name", college.getName().trim());
        if (college.getId() != null) {
            dup.ne("id", college.getId());
        }
        if (collegeMapper.selectCount(dup) > 0) {
            throw new BizException("学院名称已存在");
        }
        college.setName(college.getName().trim());
        if (college.getEnabled() == null) {
            college.setEnabled(true);
        }
        if (college.getId() == null) {
            Integer max = collegeMapper.selectObjs(new QueryWrapper<College>()
                    .select("MAX(sort_no)"))
                    .stream().filter(Objects::nonNull).findFirst().map(o -> ((Number) o).intValue()).orElse(0);
            college.setSortNo(max + 1);
            collegeMapper.insert(college);
        } else {
            if (college.getSortNo() == null) {
                college.setSortNo(0);
            }
            collegeMapper.updateById(college);
        }
        return college;
    }

    public void deleteCollege(Long id) {
        AuthContext.requireAdmin();
        if (majorMapper.selectCount(new QueryWrapper<Major>().eq("college_id", id)) > 0) {
            throw new BizException("该学院下还有专业，无法删除");
        }
        if (collegeMapper.deleteById(id) == 0) {
            throw new BizException("学院不存在");
        }
    }

    // ===== 专业 =====

    public List<Major> listMajors(Long collegeId, String collegeName, Boolean enabled) {
        AuthContext.requireAdmin();
        Long targetCollegeId = collegeId;
        if (targetCollegeId == null && collegeName != null && !collegeName.trim().isEmpty()) {
            College target = collegeMapper.selectOne(new QueryWrapper<College>()
                    .eq("name", collegeName.trim())
                    .last("LIMIT 1"));
            if (target == null) {
                return List.of();
            }
            targetCollegeId = target.getId();
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
        return majorMapper.selectList(wrapper);
    }

    public Major saveMajor(Major major) {
        AuthContext.requireAdmin();
        if (major.getName() == null || major.getName().trim().isEmpty()) {
            throw new BizException("专业名称不能为空");
        }
        if (major.getCollegeId() == null) {
            throw new BizException("请选择所属学院");
        }
        if (collegeMapper.selectById(major.getCollegeId()) == null) {
            throw new BizException("所属学院不存在");
        }
        QueryWrapper<Major> dup = new QueryWrapper<Major>()
                .eq("college_id", major.getCollegeId())
                .eq("name", major.getName().trim());
        if (major.getId() != null) {
            dup.ne("id", major.getId());
        }
        if (majorMapper.selectCount(dup) > 0) {
            throw new BizException("该学院下已存在同名专业");
        }
        major.setName(major.getName().trim());
        if (major.getEnabled() == null) {
            major.setEnabled(true);
        }
        if (major.getId() == null) {
            Integer max = majorMapper.selectObjs(new QueryWrapper<Major>()
                    .eq("college_id", major.getCollegeId())
                    .select("MAX(sort_no)"))
                    .stream().filter(Objects::nonNull).findFirst().map(o -> ((Number) o).intValue()).orElse(0);
            major.setSortNo(max + 1);
            majorMapper.insert(major);
        } else {
            if (major.getSortNo() == null) {
                major.setSortNo(0);
            }
            majorMapper.updateById(major);
        }
        return major;
    }

    public void deleteMajor(Long id) {
        AuthContext.requireAdmin();
        if (classMapper.selectCount(new QueryWrapper<SchoolClass>().eq("major_id", id)) > 0) {
            throw new BizException("该专业下还有班级，无法删除");
        }
        if (majorMapper.deleteById(id) == 0) {
            throw new BizException("专业不存在");
        }
    }

    // ===== 班级 =====

    public List<SchoolClass> listClasses(Long majorId, Boolean enabled) {
        AuthContext.requireAdmin();
        QueryWrapper<SchoolClass> wrapper = new QueryWrapper<SchoolClass>()
                .orderByAsc("major_id")
                .orderByAsc("sort_no")
                .orderByAsc("id");
        if (majorId != null) {
            wrapper.eq("major_id", majorId);
        }
        if (enabled != null) {
            wrapper.eq("is_enabled", enabled);
        }
        return classMapper.selectList(wrapper);
    }

    public SchoolClass saveClass(SchoolClass schoolClass) {
        AuthContext.requireAdmin();
        if (schoolClass.getName() == null || schoolClass.getName().trim().isEmpty()) {
            throw new BizException("班级名称不能为空");
        }
        if (schoolClass.getMajorId() == null) {
            throw new BizException("请选择所属专业");
        }
        if (majorMapper.selectById(schoolClass.getMajorId()) == null) {
            throw new BizException("所属专业不存在");
        }
        QueryWrapper<SchoolClass> dup = new QueryWrapper<SchoolClass>()
                .eq("major_id", schoolClass.getMajorId())
                .eq("name", schoolClass.getName().trim());
        if (schoolClass.getId() != null) {
            dup.ne("id", schoolClass.getId());
        }
        if (classMapper.selectCount(dup) > 0) {
            throw new BizException("该专业下已存在同名班级");
        }
        schoolClass.setName(schoolClass.getName().trim());
        if (schoolClass.getEnabled() == null) {
            schoolClass.setEnabled(true);
        }
        if (schoolClass.getId() == null) {
            Integer max = classMapper.selectObjs(new QueryWrapper<SchoolClass>()
                    .eq("major_id", schoolClass.getMajorId())
                    .select("MAX(sort_no)"))
                    .stream().filter(Objects::nonNull).findFirst().map(o -> ((Number) o).intValue()).orElse(0);
            schoolClass.setSortNo(max + 1);
            classMapper.insert(schoolClass);
        } else {
            if (schoolClass.getSortNo() == null) {
                schoolClass.setSortNo(0);
            }
            classMapper.updateById(schoolClass);
        }
        return schoolClass;
    }

    public void deleteClass(Long id) {
        AuthContext.requireAdmin();
        if (classMapper.deleteById(id) == 0) {
            throw new BizException("班级不存在");
        }
    }
}
