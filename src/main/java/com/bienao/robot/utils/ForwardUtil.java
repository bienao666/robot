package com.bienao.robot.utils;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.MessageFromType;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ForwardUtil {

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private SystemParamUtil systemParamUtil;

    public void forward(Integer messageType,String message, String to, Integer toType){
        if (toType.equals(MessageFromType.wxq)){
            JSONObject content = new JSONObject();
            content.put("robot_wxid",systemParamUtil.querySystemParam("ROBORTWXID"));
            content.put("from_group",to);
            switch (messageType){
                case 1:
                    weChatUtil.sendTextMsg(message,content);
                    break;
                case 2002:
                    weChatUtil.sendXmlMsg(message,content);
                    break;
                default:
                    log.info("未知类型：{}",messageType);
            }
        }
    }
}
