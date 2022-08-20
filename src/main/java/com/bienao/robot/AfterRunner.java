package com.bienao.robot;

import cn.hutool.cache.Cache;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.service.weixin.WxService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
        TreeMap<Integer, String> functionList = new TreeMap<>();
        functionList.put(1,"比价");
        functionList.put(2,"青龙");
        functionList.put(3,"摸鱼");
        functionList.put(4,"微博");
        functionList.put(5,"举牌");
        functionList.put(6,"天气");
        functionList.put(7,"买家秀");
        redis.put("functionList",JSONObject.toJSONString(functionList));
    }
}
