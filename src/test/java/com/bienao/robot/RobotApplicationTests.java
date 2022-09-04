package com.bienao.robot;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;

@SpringBootTest
class RobotApplicationTests {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WxService wxService;

    @Autowired
    private WeChatUtil weChatUtil;

    @Test
    public void test1() throws Exception {
        HashSet<String> set = new HashSet<>();
        boolean add = set.add("1");
        System.out.println(add);
        add = set.add("1");
        System.out.println(add);
    }

}
