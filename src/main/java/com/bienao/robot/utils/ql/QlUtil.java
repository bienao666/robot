package com.bienao.robot.utils.ql;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class QlUtil {

    //获取用户秘钥
    public JSONObject getToken(String url,String clientId,String clientSecret){
        if (url.endsWith("/")){
            url = url+"/";
        }
        String resStr = HttpRequest.get(url + "open/auth/token?client_id=" + clientId +"&client_secret=" + clientSecret)
                .execute().body();
        if (StringUtils.isEmpty(resStr)){
            log.info("青龙获取用户秘钥失败");
            return null;
        }
        JSONObject res = JSONObject.parseObject(resStr);
        if (!res.getString("code").equals("200")){
            log.info("青龙获取用户秘钥失败");
            return null;
        }
        return res.getJSONObject("data");
    }

    /**
     * 获取所有环境变量详情
     */
    public List<JSONObject> getEnvs(String url,String tokenType,String token){
        if (url.endsWith("/")){
            url = url+"/";
        }
        String resStr = HttpRequest.get(url + "open/envs")
                .header("Authorization",tokenType + " " + token)
                .execute().body();
        if (StringUtils.isEmpty(resStr)){
            log.info("青龙获取所有环境变量详情失败");
            return null;
        }
        JSONObject res = JSONObject.parseObject(resStr);
        if (!res.getString("code").equals("200")){
            log.info("青龙获取所有环境变量详情失败");
            return null;
        }
        String data = res.getString("data");
        return JSON.parseArray(data,JSONObject.class);
    }
}
