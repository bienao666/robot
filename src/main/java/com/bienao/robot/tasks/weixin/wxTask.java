package com.bienao.robot.tasks.weixin;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.service.weixin.WxService;
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
        content.put("from_group","18074044987@chatroom");
        content.put("robot_wxid","wxid_eyo76pqzpsvz12");
//        content.put("from_wxid","wxid_hwzi277usnv522");
        wxServicel.handleWeiBo(content);
    }

    /**
     * 饿了么定时推送
     */
    /*@Scheduled(cron = "0 40 10 * * ?")
    public void timeHandleELM() {
        JSONObject content = new JSONObject();
        content.put("from_group","18074044987@chatroom");
        content.put("robot_wxid","wxid_eyo76pqzpsvz12");
        wxServicel.timeHandleELM(content);
    }*/
}
