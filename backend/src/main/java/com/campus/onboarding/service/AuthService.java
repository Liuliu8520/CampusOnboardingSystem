package com.campus.onboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.onboarding.common.BizException;
import com.campus.onboarding.dto.LoginRequest;
import com.campus.onboarding.dto.LoginResponse;
import com.campus.onboarding.entity.Admin;
import com.campus.onboarding.entity.Student;
import com.campus.onboarding.mapper.AdminMapper;
import com.campus.onboarding.mapper.StudentMapper;
import com.campus.onboarding.security.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;

    @Value("${app.captcha.fixed-code}")
    private String fixedCaptcha;

    public AuthService(StudentMapper studentMapper,
                       AdminMapper adminMapper,
                       BCryptPasswordEncoder passwordEncoder,
                       TokenUtil tokenUtil) {
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
    }

    public LoginResponse login(LoginRequest request) {
        if (!fixedCaptcha.equals(request.captcha())) {
            throw new BizException("验证码错误");
        }
        if ("ADMIN".equalsIgnoreCase(request.role())) {
            Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", request.username()));
            if (admin == null || !passwordEncoder.matches(request.password(), admin.getPassword())) {
                throw new BizException("管理员账号或密码错误");
            }
            String token = tokenUtil.generate(admin.getUsername(), "ADMIN", admin.getName());
            return new LoginResponse(token, "ADMIN", admin.getUsername(), admin.getName());
        }
        if ("STUDENT".equalsIgnoreCase(request.role())) {
            Student student = studentMapper.selectOne(new QueryWrapper<Student>().eq("student_id", request.username()));
            if (student == null || !passwordEncoder.matches(request.password(), student.getPassword())) {
                throw new BizException("学号或密码错误");
            }
            String token = tokenUtil.generate(student.getStudentId(), "STUDENT", student.getName());
            return new LoginResponse(token, "STUDENT", student.getStudentId(), student.getName());
        }
        throw new BizException("登录角色不正确");
    }
}
