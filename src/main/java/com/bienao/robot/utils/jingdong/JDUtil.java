package com.bienao.robot.utils.jingdong;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JDUtil {

    /**
     * 查询当前ck信息
     *
     * @param ck
     * @return
     */
    public static JSONObject queryDetail(String ck) {
        String result = HttpRequest.get("https://me-api.jd.com/user_new/info/GetJDUserInfoUnion")
                .header("Host", "me-api.jd.com")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .header("Cookie", ck)
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-cn")
                .header("Referer", "https://home.m.jd.com/myJd/newhome.action?sceneval=2&ufc=&")
                .header("Accept-Encoding", "gzip, deflate, br")
                .execute().body();
        log.info("查询当前ck结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("京东服务器返回空数据");
            throw new RuntimeException("京东服务器返回空数据");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            if ("1001".equals(resultObject.getString("retcode"))) {
                //ck过期
                return null;
            }
            if ("0".equals(resultObject.getString("retcode")) && resultObject.getJSONObject("data").getJSONObject("userInfo") != null) {
                return resultObject.getJSONObject("data").getJSONObject("userInfo");
            }
            return new JSONObject();
        }
    }

    /**
     * 查询当前ck信息
     *
     * @param ck
     * @return
     */
    public static boolean isVaild(String ck) {
        String result = HttpRequest.get("https://me-api.jd.com/user_new/info/GetJDUserInfoUnion")
                .header("Host", "me-api.jd.com")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .header("Cookie", ck)
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-cn")
                .header("Referer", "https://home.m.jd.com/myJd/newhome.action?sceneval=2&ufc=&")
                .header("Accept-Encoding", "gzip, deflate, br")
                .execute().body();
        log.info("查询当前ck结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("京东服务器返回空数据");
            return true;
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            if ("1001".equals(resultObject.getString("retcode"))) {
                //ck过期
                return false;
            }
            return true;
        }
    }

    /**
     * 过期ck
     * @param ck
     */
    public static void expire(String ck){
        HttpRequest.get("https://plogin.m.jd.com/cgi-bin/ml/mlogout?appid=300&returnurl=https%3A%2F%2Fm.jd.com%2F")
                .header("authority", "plogin.m.jd.com")
                .header("User-Agent", "GetUserAgentUtil.getUserAgent()")
                .header("cookie", ck)
                .execute()
                .body();

    }
}
