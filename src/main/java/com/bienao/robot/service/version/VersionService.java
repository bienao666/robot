package com.bienao.robot.service.version;

import com.alibaba.fastjson.JSONObject;

public interface VersionService {

    JSONObject queryVersion();

    JSONObject queryNewestVersion();
}
