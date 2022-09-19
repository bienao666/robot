package com.bienao.robot;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;

@SpringBootTest
class RobotApplicationTests {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WxService wxService;

    @Autowired
    private WeChatUtil weChatUtil;

    @Test
    public void test1() {
        while (true){
//            JSONObject body = new JSONObject();
//            body.put("",1);
//            body.put("",1);
//            body.put("",1);
//            body.put("",1);
//            body.put("",1);
//            body.put("","1");
            String resStr = HttpRequest.post("https://cat-match.easygame2021.com/sheep/v1/game/game_over_ex")
//                    .header("Host","cat-match.easygame2021.com")
//                    .header("Connection","keep-alive")
                    .header("t","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2OTQ2OTAxNTUsIm5iZiI6MTY2MzU4Nzk1NSwiaWF0IjoxNjYzNTg2MTU1LCJqdGkiOiJDTTpjYXRfbWF0Y2g6bHQxMjM0NTYiLCJvcGVuX2lkIjoiIiwidWlkIjoyMzU3ODEzMTMsImRlYnVnIjoiIiwibGFuZyI6IiJ9.lweIyNQSX36nrD_kR5Or2A9_jKaAWRNAHIFeNhZnfMA")
//                    .header("content-type","application/json")
//                    .header("Accept-Encoding","gzip,compress,br,deflate")
//                    .header("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 15_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.27(0x18001b36) NetType/4G Language/zh_CN")
//                    .header("Referer","https://servicewechat.com/wx141bfb9b73c970a9/14/page-frame.html")
                    .timeout(3000)
                    .form("rank_score",1)
                    .form("rank_state",1)
                    .form("rank_role",1)
                    .form("rank_time",1)
                    .form("skin",1)
                    .form("MatchPlayInfo","1")
//                    .body(body.toJSONString())
                    .execute().body();
        }
    }
}
