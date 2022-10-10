package com.bienao.robot;

import cn.hutool.http.HttpRequest;
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
    public void test1() {
        JSONObject jsonObject = new JSONObject();
        JSONObject content = new JSONObject();
        content.put("msg","摸鱼");
        content.put("type",1);
        jsonObject.put("content",content);
        wxService.handleMessage(jsonObject);
    }
}
