package com.bienao.robot.tasks.qinglong;


import com.bienao.robot.entity.WireActivityEntity;
import com.bienao.robot.mapper.WirelistMapper;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
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

    @Autowired
    private QlService qlService;

    @Autowired
    private SystemParamUtil systemParamUtil;


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

    /**
     * 设置小车头
     * 5分钟设置一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void setSmallHead(){
        qlService.setSmallHead();
    }

    /**
     * 韭菜友好设置
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void leekFriendly(){
        String leekFriendly = systemParamUtil.querySystemParam("LEEKFRIENDLY");
        if ("是".equals(leekFriendly)){
            qlService.leekFriendly();
        }
    }

    /**
     * 青龙检测
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkQl(){
        qlService.checkQl();
    }

    /**
     * ck检测
     */
    @Scheduled(cron = "0 30 10,22 * * ?")
    public void checkck(){
        qlService.checkCk();
    }

    /**
     * 多青龙 ck分布优化
     */
    @Scheduled(cron = "0 30 */3 * * ?")
    public void autoAdjust(){
        String leekFriendly = systemParamUtil.querySystemParam("QLCKAUTOADJUST");
        if ("是".equals(leekFriendly)){
            qlService.autoAdjust();
        }
    }
}
