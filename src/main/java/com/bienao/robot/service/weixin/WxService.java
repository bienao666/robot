package com.bienao.robot.service.weixin;

import com.alibaba.fastjson.JSONObject;

public interface WxService {

    void handleMessage(JSONObject jsonObject);

    void timeHandleELM();

    void handleWeiBo(JSONObject content);

    void handleMoYu(JSONObject content);

    void handleWater();
}
