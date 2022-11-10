package com.bienao.robot.controller;

import cn.hutool.cache.Cache;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.redis.Redis;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    @GetMapping("/test")
    public void test() {
        Cache<String, String> redis = Redis.redis;
        redis.put("test","true",10*1000L);
        for (int i = 0; i < 30; i++) {
            String test = redis.get("test", false);
            System.out.println(StringUtils.isEmpty(test) +" - " + (i+1));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
