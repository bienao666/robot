package com.bienao.robot.tasks.ylgy;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.service.ylgy.YlgyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public void brush() {
        //查询待刷账号
        List<JSONObject> list = ylgyService.query();
        for (JSONObject ylgy : list) {
            String id = ylgy.getString("id");
            String uid = ylgy.getString("uid");
            String token = ylgy.getString("token");
            Integer time = 10000;
            String timesStr = ylgy.getString("times");
            if (StringUtils.isEmpty(timesStr)){
                time = Integer.parseInt(timesStr);
            }
            ylgyService.handleBrush(id,uid,token,time);
        }
    }
}
