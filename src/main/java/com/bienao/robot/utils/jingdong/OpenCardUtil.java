package com.bienao.robot.utils.jingdong;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class OpenCardUtil {

    public static JSONObject getMyPing(String UUID,String ADID,String activityUrl,String ck,String activityShopId,String token){
        HttpResponse execute = HttpRequest.post("https://lzdz1-isv.isvjcloud.com/customer/getMyPing")
                .header("Host", "lzdz1-isv.isvjcloud.com")
                .header("Accept", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Accept-Language", "zh-cn")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Origin", "https://lzdz1-isv.isvjcloud.com")
                .header("User-Agent", "jdapp;iPhone;9.5.4;13.6;" + UUID + ";network/wifi;ADID/" + ADID + ";model/iPhone10,3;addressid/0;appBuild/167668;jdSupportDarkMode/0;Mozilla/5.0 (iPhone; CPU iPhone OS 13_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148;supportJDSHWK/1")
                .header("Connection", "keep-alive")
                .header("Referer", activityUrl)
                .header("Cookie", ck)
                .body("userId=" + activityShopId + "&token=" + token + "&fromType=APP&riskType=1")
                .timeout(10000)
                .execute();
        String result = execute.body();
        Map<String, List<String>> headers = execute.headers();
        if (StringUtils.isEmpty(result) || result.contains("403")){
            log.info("查询{}Ping失败",ck);
            throw new RuntimeException("调用京东接口失败，可能ip已黑，请尝试更换ip再试");
        }else {
            JSONObject resultJsonObject = JSONObject.parseObject(result);
            String secretPin = resultJsonObject.getJSONObject("data").getString("secretPin");
            String pin = resultJsonObject.getJSONObject("data").getString("nickname");
            ck = ck + headers.get("set-cookie").get(0).split(";")[0];
            //todo
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cookie",ck);
            jsonObject.put("secretPin",secretPin);
            jsonObject.put("pin",pin);
            return jsonObject;
        }
    }
}
