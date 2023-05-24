package com.bienao.robot.tasks;

import com.bienao.robot.service.jetbrains.JetbrainsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class JetbrainsTask {

    @Autowired
    private JetbrainsService jetbrainsService;

    /**
     * 定时爬取Jetbrains激活服务器
     */
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void reptile(){
        jetbrainsService.reptile();
    }
}
