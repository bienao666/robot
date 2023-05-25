package com.bienao.robot.service.jetbrains;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface JetbrainsService {
    void reptile();

    JSONObject getValidUrls();

    void addUrls(String urls);
}
