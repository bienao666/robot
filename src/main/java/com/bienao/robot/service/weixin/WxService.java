package com.bienao.robot.service.weixin;

import com.alibaba.fastjson.JSONObject;

public interface WxService {

    void handleMessage(JSONObject jsonObject);

    void timeHandleELM(JSONObject content);

    void handleWeiBo(JSONObject content);
}
