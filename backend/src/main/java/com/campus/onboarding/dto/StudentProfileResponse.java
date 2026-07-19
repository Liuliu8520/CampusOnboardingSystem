package com.campus.onboarding.dto;

import com.campus.onboarding.entity.QualificationModification;
import com.campus.onboarding.entity.Student;

import java.util.List;
import java.util.Map;

public record StudentProfileResponse(
        Student student,
        int currentStep,
        String currentStepName,
        Map<String, Object> dorm,
        List<QualificationModification> modifications
) {
}
