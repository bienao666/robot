package com.bienao.robot.service.weixin;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Result;

public interface WxService {

    void handleMessage(JSONObject jsonObject);

    void timeHandleELM();

    void timeHandleMY();

    void timeHandleWB();

    void handleWeiBo(JSONObject content);

    void handleMoYu(JSONObject content);

    void handleWater();

    /**
     * 修改微信步数
     * @param username
     * @param password
     * @param step
     * @param minstep
     * @param maxstep
     * @param expiryTime
     * @return
     */
    Result updateStep(String username, String password, Integer step, boolean istime, Integer minstep, Integer maxstep, String expiryTime);

    void timeWxbs();

    void timeHandleZfbHb();

    void handleQueryJdAssets(JSONObject content);
}
