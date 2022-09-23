package com.bienao.robot.tasks.weixin;

import cn.hutool.cache.Cache;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class wxTask {

    @Autowired
    private WxService wxServicel;

    @Autowired
    private SystemParamUtil systemParamUtil;

    private Cache<String, String> redis = WXConstant.redis;

    /**
     * 消费微信信息
     */
    @Scheduled(cron = "* * * * * ?")
    public void handleMessage() {
        EvictingQueue<JSONObject> messageLists = WXConstant.messageList;
        JSONObject message = messageLists.poll();
        if (message != null) {
            wxServicel.handleMessage(message);
        }
    }

    /**
     * 推送微博
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void timeWeiBo() {
        wxServicel.timeHandleWB();
    }

    /**
     * 推送摸鱼
     */
    @Scheduled(cron = "0 0 15 * * ?")
    public void timeMoYu() {
        wxServicel.timeHandleMY();
    }

    /**
     * 喝水贴心小助手
     */
    @Scheduled(cron = "0 0 8/2 * * ?")
    public void timeWater() {
        wxServicel.handleWater();
    }

    /**
     * 饿了么定时推送
     */
    @Scheduled(cron = "0 40 10,17 * * ?")
    public void timeHandleELM() {
        wxServicel.timeHandleELM();
    }

    /**
     * 支付宝红包定时推送
     */
    @Scheduled(cron = "0 8 * * * ?")
    public void timeHandleZfbHb() {
        wxServicel.timeHandleZfbHb();
    }

    /**
     * 微信步数
     */
    @Scheduled(cron = "0 0 13,15 * * ?")
    public void timeWxbs() {
        wxServicel.timeWxbs();
    }
}
