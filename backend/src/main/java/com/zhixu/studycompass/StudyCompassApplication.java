package com.zhixu.studycompass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zhixu.studycompass.mapper")
@SpringBootApplication
public class StudyCompassApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyCompassApplication.class, args);
    }
}

