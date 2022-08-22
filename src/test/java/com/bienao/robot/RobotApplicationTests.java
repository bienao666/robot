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

    @Autowired
    private WeChatUtil weChatUtil;

    @Test
    public void test1() throws Exception {
        JSONObject content = new JSONObject();
        content.put("robot_wxid","wxid_eyo76pqzpsvz12");
        content.put("from_wxid","wxid_hwzi277usnv522");
        weChatUtil.sendImageMsg("http://i.imgtg.com/2022/08/09/AEf1U.jpg", content);
    }

}
