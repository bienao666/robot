package com.bienao.robot;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.service.weixin.WxService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.TreeMap;

@Component
@Slf4j
public class AfterRunner implements ApplicationRunner {

    @Autowired
    private WxService wxService;

    private Cache<String, String> redis = WXConstant.redis;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //启动监听
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("开始监听微信信息。。。");
                while (true){
                    EvictingQueue<JSONObject> messageLists = WXConstant.messageList;
                    if (messageLists.size()>0){
                        JSONObject message = messageLists.poll();
                        if (message != null) {
                            wxService.handleMessage(message);
                        }
                    }
                }
            }
        }).start();
        //添加功能列表
        String msg = "菜单列表：\r\n";
        msg += "————青龙区————\r\n";
        msg += "           青龙        \r\n";
        msg += "————功能区————\r\n";
        msg += "           比价  |  油价  \r\n";
        msg += "           监控茅台洋河    \r\n";
        msg += "————娱乐区————\r\n";
        msg += "           摸鱼  |  微博  \r\n";
        msg += "           举牌  |  天气  \r\n";
        msg += "           买家秀     \r\n";
        redis.put("functionList",msg);
    }
}
