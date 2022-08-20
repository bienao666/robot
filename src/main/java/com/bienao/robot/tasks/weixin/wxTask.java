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
        JSONObject content = new JSONObject();
        content.put("msg","微博");
        content.put("from_group",systemParamUtil.querySystemParam("SENDWEIBOLIST"));
        content.put("robot_wxid",systemParamUtil.querySystemParam("ROBORTWXID"));
        wxServicel.handleWeiBo(content);
    }

    /**
     * 推送摸鱼
     */
    @Scheduled(cron = "0 0 15 * * ?")
    public void timeMoYu() {
        JSONObject content = new JSONObject();
        content.put("msg","摸鱼");
        content.put("from_group",systemParamUtil.querySystemParam("SENDMOYULIST"));
        content.put("robot_wxid",systemParamUtil.querySystemParam("ROBORTWXID"));
        wxServicel.handleMoYu(content);
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
    /*@Scheduled(cron = "0 40 10 * * ?")
    public void timeHandleELM() {
        JSONObject content = new JSONObject();
        content.put("from_group",systemParamUtil.querySystemParam("SENDELMLIST"));
        content.put("robot_wxid",systemParamUtil.querySystemParam("ROBORTWXID"));
        wxServicel.timeHandleELM(content);
    }*/
}
