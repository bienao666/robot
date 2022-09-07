package com.bienao.robot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@MapperScan("com.bienao.robot.mapper")
@EnableScheduling //开启定时任务
@Validated
public class RobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotApplication.class, args);
    }

}
