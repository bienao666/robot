package com.bienao.robot.tasks.qinglong;


import com.bienao.robot.entity.WireActivityEntity;
import com.bienao.robot.mapper.WirelistMapper;
import com.bienao.robot.service.ql.WireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class QlTask {

    @Autowired
    private WireService wireService;

    @Autowired
    private WirelistMapper wirelistMapper;


    /**
     * 执行线报
     */
    @Scheduled(cron = "0 * * * * ?")
    public void handleWire() {
        //查询近十条未执行的线报
        List<WireActivityEntity> wireActivityEntities = wirelistMapper.queryToBeExecutedActivity();
        for (WireActivityEntity wireActivityEntity : wireActivityEntities) {
            wireService.handleWire(wireActivityEntity.getId(),wireActivityEntity.getScript(),wireActivityEntity.getContent());
        }
    }

    /**
     * 清理无效数据
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void clear(){
        wirelistMapper.clear();
    }
}
