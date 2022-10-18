package com.bienao.robot.service.impl.version;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.service.version.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VersionServiceImpl implements VersionService {

    @Value("${AUTH_ADDR}")
    private String authAddr;

    @Value("${VERSION}")
    private String version;

    @Override
    public JSONObject queryVersion() {
        String curVersion = version;
        JSONObject version = new JSONObject();
        version.put("curVersion",curVersion);
        try {
            String resStr = HttpRequest.get(authAddr+"/robot/version/queryNewestVersion")
                    .timeout(500)
                    .execute().body();
            JSONObject res = JSONObject.parseObject(resStr);
            if (res.getInteger("code")==200){
                version.put("newestVersion",res.getJSONObject("data").getString("newestVersion"));
            }
        } catch (Exception e) {
            log.error("查询最新版本异常",e);
            version.put("newestVersion",curVersion);
        }
        return version;
    }

    @Override
    public JSONObject queryNewestVersion() {
        JSONObject res = new JSONObject();
        res.put("newestVersion",version);
        return res;
    }
}
