package com.campus.onboarding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.onboarding.dto.StudentFeeStatus;

import java.time.LocalDateTime;
import java.util.List;

@TableName("students")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String name;
    private String gender;
    private Long collegeId;
    private Long majorId;
    private Long classId;
    private String phone;
    private String idCard;
    private String address;
    private String password;
    @TableField("is_paid")
    private Boolean paid;
    @TableField("is_verified")
    private Boolean verified;
    @TableField("is_checked_in")
    private Boolean checkedIn;
    private Long bedId;
    @TableField(exist = false)
    private List<StudentFeeStatus> paymentStatuses;
    @TableField(exist = false)
    private Integer requiredFeePaidCount;
    @TableField(exist = false)
    private Integer requiredFeeTotal;
    @TableField(exist = false)
    private String college;
    @TableField(exist = false)
    private String major;
    @TableField(exist = false)
    private String className;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Boolean getCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public List<StudentFeeStatus> getPaymentStatuses() {
        return paymentStatuses;
    }

    public void setPaymentStatuses(List<StudentFeeStatus> paymentStatuses) {
        this.paymentStatuses = paymentStatuses;
    }

    public Integer getRequiredFeePaidCount() {
        return requiredFeePaidCount;
    }

    public void setRequiredFeePaidCount(Integer requiredFeePaidCount) {
        this.requiredFeePaidCount = requiredFeePaidCount;
    }

    public Integer getRequiredFeeTotal() {
        return requiredFeeTotal;
    }

    public void setRequiredFeeTotal(Integer requiredFeeTotal) {
        this.requiredFeeTotal = requiredFeeTotal;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
