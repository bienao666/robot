package com.bienao.robot.utils;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class YlgyUtils {

    public static String getYlgyToken(String uid){
        String wxOpenId = "";
        //获取wxUnionId
        for (int i = 0; i < 20; i++) {
            if (StringUtils.isEmpty(wxOpenId)){
                log.info("开始第"+(i+1)+"次获取羊了个羊用户："+uid+" 的token");
                String resStr = null;
                try {
                    resStr = HttpRequest.get("https://cat-match.easygame2021.com/sheep/v1/game/user_info?uid=" + uid)
                            .header("Accept","*/*")
                            .header("Accept-Encoding","gzip,compress,br,deflate")
                            .header("Connection","keep-alive")
                            .header("content-type","application/json")
                            .header("Host","cat-match.easygame2021.com")
                            .header("Connection","keep-alive")
                            .header("t","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2OTQ1MDI0NDUsIm5iZiI6MTY2MzQwMDI0NSwiaWF0IjoxNjYzMzk4NDQ1LCJqdGkiOiJDTTpjYXRfbWF0Y2g6bHQxMjM0NTYiLCJvcGVuX2lkIjoiIiwidWlkIjo0NTk0MjYwMiwiZGVidWciOiIiLCJsYW5nIjoiIn0.1lXIcb1WL_SdsXG5N_i1drjjACRhRZUS2uadHlT6zIY")
                            .header("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 15_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.27(0x18001b36) NetType/4G Language/zh_CN")
                            .header("Referer","https://servicewechat.com/wx141bfb9b73c970a9/14/page-frame.html")
                            .timeout(3000)
                            .execute().body();
                } catch (Exception e) {

                }
                if (StringUtils.isNotEmpty(resStr)){
                    JSONObject res = JSONObject.parseObject(resStr);
                    if (0==res.getInteger("err_code")){
                        wxOpenId = res.getJSONObject("data").getString("wx_open_id");
                        break;
                    }
                }
            }else {
                break;
            }
            //休息休息一下
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isNotEmpty(wxOpenId)){
            //获取token
            String resStr = null;
            for (int i = 0; i < 20; i++) {
                try {
                    resStr = HttpRequest.post("https://cat-match.easygame2021.com/sheep/v1/user/login_tourist")
                            .form("uuid", wxOpenId)
                            .timeout(3000)
                            .execute().body();
                } catch (HttpException e) {

                }
                if (StringUtils.isNotEmpty(resStr)){
                    JSONObject res = JSONObject.parseObject(resStr);
                    if (0==res.getInteger("err_code")){
                        return res.getJSONObject("data").getString("token");
                    }
                }
                //休息休息一下
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
