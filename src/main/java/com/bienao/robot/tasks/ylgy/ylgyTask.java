package com.bienao.robot.tasks.ylgy;

import com.bienao.robot.service.ylgy.YlgyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 羊了个羊定时任务
 */
@Component
@Slf4j
public class ylgyTask {

    @Autowired
    private YlgyService ylgyService;
    /**
     * 羊了个羊代刷
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void timeYlgy() {
        ylgyService.brush();
    }
}
