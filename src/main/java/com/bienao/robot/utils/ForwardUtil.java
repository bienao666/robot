package com.bienao.robot.utils;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.MessageFromType;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ForwardUtil {

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private SystemParamUtil systemParamUtil;

    public void forward(String message, String to, Integer toType){
        if (toType.equals(MessageFromType.wxq)){
            JSONObject content = new JSONObject();
            content.put("robot_wxid",systemParamUtil.querySystemParam("ROBORTWXID"));
            content.put("from_group",to);
            weChatUtil.sendTextMsg(message,content);
        }
    }
}
