package com.zinidata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Enterprise Template 메인 애플리케이션
 * 
 * @author ZiniData
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.zinidata")
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan("com.zinidata.audit.mapper, com.zinidata.domain.**.mapper")
public class ZiniDataApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ZiniDataApplication.class, args);
    }
}