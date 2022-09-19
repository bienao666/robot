package com.bienao.robot.service.ylgy;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface YlgyService {
    List<JSONObject> query();

    void handleBrush(String id, String uid, String token, Integer times);
}
