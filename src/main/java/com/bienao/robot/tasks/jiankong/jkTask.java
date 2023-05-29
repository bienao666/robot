package com.bienao.robot.tasks.jiankong;

import com.bienao.robot.service.jiankong.Jiankong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 监控定时任务
 */
@Component
@Slf4j
public class jkTask {

    @Autowired
    private Jiankong jiankong;

    /**
     * 设么值得买监控洋河
     */
//    @Scheduled(cron = "0 0/5 * * * ? ")
    public void timeJiankongSmzdmyh() {
        jiankong.jiankongsmzdmyh();
        jiankong.jiankongsmzdmmt();
    }

    /**
     * 重置推送列表
     */
//    @Scheduled(cron = "0 0 0 * * ?")
    public void resetSendList() {
        jiankong.resetSendList();
    }
}
