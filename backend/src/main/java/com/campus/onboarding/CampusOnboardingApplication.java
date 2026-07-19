package com.campus.onboarding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.campus.onboarding.mapper")
@SpringBootApplication
public class CampusOnboardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusOnboardingApplication.class, args);
    }
}
