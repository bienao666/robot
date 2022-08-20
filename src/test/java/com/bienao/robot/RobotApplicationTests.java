package com.bienao.robot;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RobotApplicationTests {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WxService wxService;

    @Test
    public void test1() throws Exception {
        JSONObject content = new JSONObject();
        content.put("robot_wxid", "wxid_eyo76pqzpsvz12");
        content.put("from_wxid", "zhaochuanzheng521");
        content.put("msg", "北京 密云天气");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content",content);
        wxService.handleMessage(jsonObject);
    }

}
