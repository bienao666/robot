package com.bienao.robot.service.ql;

import com.alibaba.fastjson.JSONObject;

public interface QlService {

    /**
     * 一键设置大车头
     * @return
     */
    public boolean oneKeyBigHead(JSONObject content);
}
