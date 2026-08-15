package com.campus.onboarding.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.onboarding.common.BizException;
import com.campus.onboarding.dto.ChangePasswordRequest;
import com.campus.onboarding.dto.LoginRequest;
import com.campus.onboarding.dto.LoginResponse;
import com.campus.onboarding.entity.Admin;
import com.campus.onboarding.entity.Student;
import com.campus.onboarding.mapper.AdminMapper;
import com.campus.onboarding.mapper.StudentMapper;
import com.campus.onboarding.security.AuthContext;
import com.campus.onboarding.security.AuthUser;
import com.campus.onboarding.security.TokenUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;
    private final CaptchaService captchaService;

    public AuthService(StudentMapper studentMapper,
                       AdminMapper adminMapper,
                       BCryptPasswordEncoder passwordEncoder,
                       TokenUtil tokenUtil,
                       CaptchaService captchaService) {
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
        this.captchaService = captchaService;
    }

    public LoginResponse login(LoginRequest request) {
        if (!captchaService.verify(request.captchaId(), request.captcha())) {
            throw new BizException("验证码错误或已失效，请刷新后重试");
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

    /**
     * 修改当前登录账号的密码，按角色定位学生或管理员账户，校验原密码后更新。
     */
    public void changePassword(ChangePasswordRequest request) {
        AuthUser user = AuthContext.get();
        String encoded = passwordEncoder.encode(request.newPassword());
        if (user.isStudent()) {
            Student student = studentMapper.selectOne(new QueryWrapper<Student>().eq("student_id", user.account()));
            if (student == null || !passwordEncoder.matches(request.oldPassword(), student.getPassword())) {
                throw new BizException("原密码错误");
            }
            student.setPassword(encoded);
            studentMapper.updateById(student);
        } else if (user.isAdmin()) {
            Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", user.account()));
            if (admin == null || !passwordEncoder.matches(request.oldPassword(), admin.getPassword())) {
                throw new BizException("原密码错误");
            }
            admin.setPassword(encoded);
            adminMapper.updateById(admin);
        } else {
            throw new BizException(403, "当前账号不支持修改密码");
        }
    }
}
