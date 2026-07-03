package com.covex.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.covex")
@MapperScan("com.covex.service.mapper")
@EnableScheduling
public class CovexApplication {

    public static void main(String[] args) {
        SpringApplication.run(CovexApplication.class, args);
    }
}
