package com.bienao.robot;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.service.weixin.WxService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AfterRunner implements ApplicationRunner {

    @Autowired
    private WxService wxService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
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
    }
}
