package com.bienao.robot.service.impl.dianxin;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.dianxin.DianXinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAKey;
import java.util.*;

@Service
@Slf4j
public class DianXinServiceImpl implements DianXinService {


    @Override
    public Result lotteryLive(String phone , String passWord){
        String url = "https://raw.githubusercontent.com/limoruirui/Hello-World/main/telecomLiveInfo.json";
        List<JSONObject> liveInfos = null;
        try {
            String resStr = HttpRequest.get(url).timeout(5000).execute().body();
            liveInfos = JSON.parseArray(resStr,JSONObject.class);
        } catch (HttpException e) {
            url = "https://xbk.189.cn/xbkapi/lteration/index/recommend/anchorRecommend?provinceCode=01";
            String randomPhone = "1537266" + RandomUtil.randomNumbers(4);
            randomPhone = "15372662600";
            String userAgent = "CtClient;9.6.1;Android;12;SM-G9860;"
                    + Base64.encode(randomPhone.substring(5)).replace("=","").replace("+","").trim()
                    + Base64.encode(randomPhone.substring(0,5)).replace("=","").replace("+","").trim();
            String resStr = HttpRequest.get(url)
                    .header("referer", "https://xbk.189.cn/xbk/newHome?version=9.4.0&yjz=no&l=card&longitude=%24longitude%24&latitude=%24latitude%24&utm_ch=hg_app&utm_sch=hg_sh_shdbcdl&utm_as=xbk_tj&loginType=1")
                    .header("user-agent", userAgent)
                    .timeout(5000)
                    .execute().body();
            JSONObject res = JSONObject.parseObject(resStr);
            liveInfos = JSON.parseArray(res.getString("data"),JSONObject.class);
        }
        log.info("接口查询结果：{}",JSONObject.toJSONString(liveInfos));
        if (liveInfos == null && liveInfos.size()<=0){
            return Result.success();
        }

        Map<Integer,Integer> liveListInfos = new HashMap<>();
        for (JSONObject liveInfo : liveInfos) {
            Long time = DateUtil.date().getTime()/1000 - DateUtil.parseTime(liveInfo.getString("start_time")).getTime()/1000;
            if (time >0 && time < 1750){
                liveListInfos.put(liveInfo.getInteger("liveId"),liveInfo.getInteger("period"));
            }
        }
        if (liveListInfos.size() == 0){
            return Result.success("查询结束 没有近期开播的直播间");
        }
        telecomLotter(phone,passWord);

        return Result.success();
    }

    public void telecomLotter(String phone , String passWord){
        chinaTelecom(phone,passWord,true);
    }

    public void chinaTelecom(String phone , String passWord, boolean checkin){
        if (StringUtils.isEmpty(passWord) || !checkin){
            return;
        }
        JSONObject jsonObject = telecomLogin(phone, passWord);
    }

    public JSONObject telecomLogin(String phone , String passWord){
        String deviceUid = UUID.randomUUID().toString().replace("-","");
        String url = "https://appgologin.189.cn:9031/login/client/userLoginNormal";
        String timestamp = DateUtil.format(DateUtil.date(),"yyyyMMddHHmmss");
        String key = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBkLT15ThVgz6/NOl6s8GNPofd\nWzWbCkWnkaAm7O2LjkM1H7dMvzkiqdxU02jamGRHLX/ZNMCXHnPcW/sDhiFCBN18\nqFvy8g6VYb9QtroI09e176s+ZCtiv7hbin2cCTj99iUpnEloZm19lwHyo69u5UMi\nPMpq0/XKBO8lYhN/gwIDAQAB\n-----END PUBLIC KEY-----";
        JSONObject body = new JSONObject();
        JSONObject headerInfos = new JSONObject();
        headerInfos.put("code","userLoginNormal");
        headerInfos.put("timestamp",timestamp);
        headerInfos.put("broadAccount","");
        headerInfos.put("broadToken","");
        headerInfos.put("clientType","#9.6.1#channel50#iPhone 14 Pro Max#");
        headerInfos.put("shopId","20002");
        headerInfos.put("source","110003");
        headerInfos.put("sourcePassword","Sid98s");
        headerInfos.put("token","");
        headerInfos.put("userLoginName",phone);
        body.put("headerInfos",headerInfos);

        JSONObject fieldData = new JSONObject();
        fieldData.put("loginType","4");
        fieldData.put("accountType","");
        String loginAuthCipherAsymmertric = "iPhone 14 15.4." + deviceUid.substring(0,12) + "1537193037520221127015311vip153719303750$$$0.";
        fieldData.put("loginAuthCipherAsymmertric","");
        fieldData.put("deviceUid",deviceUid.substring(0,16));
        fieldData.put("phoneNum","");
        fieldData.put("isChinatelecom","0");
        fieldData.put("systemVersion","15.4.0");
        fieldData.put("authentication",passWord);

        JSONObject content = new JSONObject();
        content.put("attach","test");
        content.put("fieldData",fieldData);
        body.put("content",content);

        String resStr = HttpRequest.post(url)
                .header("user-agent", "iPhone 14 Pro Max/9.6.1")
                .execute().body();
        JSONObject res = JSONObject.parseObject(resStr);
        if (!"0000".equals(res.getJSONObject("responseData").getString("resultCode"))){
            log.info("登陆失败, 接口日志{}",resStr);
            return null;
        }
        //token
        //userId
        return res.getJSONObject("responseData").getJSONObject("data").getJSONObject("loginSuccessResult");
    }
}
