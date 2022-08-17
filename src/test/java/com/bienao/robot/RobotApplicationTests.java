package com.bienao.robot;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

@SpringBootTest
class RobotApplicationTests {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Test
    void contextLoads() {

    }

    @Test
    public void test(){
        String resStr = HttpRequest.get("http://121.43.32.165:7012/open/auth/token?client_id=6-A8PgQ3-758&client_secret=X24-F_8ZNaRgvD3AmitHbg7A")
                .execute().body();
        JSONObject res = JSONObject.parseObject(resStr);
        JSONObject data = res.getJSONObject("data");
        String token_type = data.getString("token_type");
        String token = data.getString("token");

        resStr = HttpRequest.get("http://121.43.32.165:7012/open/envs")
                .header("Authorization",token_type + " " + token)
                .execute().body();
        System.out.println(resStr);
    }

}
