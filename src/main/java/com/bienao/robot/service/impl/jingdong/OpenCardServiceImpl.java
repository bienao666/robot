package com.bienao.robot.service.impl.jingdong;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.service.jingdong.OpenCardService;
import com.bienao.robot.utils.jingdong.GetUserAgentUtil;
import com.bienao.robot.utils.jingdong.OpenCardUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenCardServiceImpl implements OpenCardService {

    @Autowired
    private JdCkMapper jdCkMapper;

    private String activityUrl = "https://lzdz1-isv.isvjcloud.com/dingzhi/joinCommon/activity/【authorNum】?activityId=【activityId】&shareUuid=【authorCode】&adsource=&shareuserid4minipg=【secretPin】&shopid=1000004065&lng=00.000000&lat=00.000000&sid=&un_area=";
    private String cookie;
    private String activityId;
    private String activityShopId;
    private String UUID;
    private String ADID;
    private String jdActivityId;
    private String venderId;
    private String activityType;
    private String ownCode;
    private String actorUuid;
    private Integer index;
    private List<JSONObject> openCardList;
    private JSONObject openCardStatus;
    private String addScore;
    private String secretPin;
    //账号昵称
    private String pin;
    //车头助力码
    private String authorCode;
    private Integer authorNum;
    private Integer randomCode;
    private String openCardActivityId;
    private String bindWithVendermessage;
    /**
     * 开卡助力ck
     */
    public void openCard(String id,String shopId,String authorCode){
        activityShopId = shopId;
        activityId = id;
        activityUrl = activityUrl.replace("【activityId】",activityId);
        activityUrl = activityUrl.replace("【authorCode】",authorCode);
        authorNum = RandomUtil.randomInt(1000000, 9999999);
        activityUrl = activityUrl.replace("【authorNum】",String.valueOf(authorNum));
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryCks(jdCkEntityQuery);
        for (int i = 0; i < jdCkEntities.size(); i++) {
            index = i +1;
            JdCkEntity jdCkEntity = jdCkEntities.get(i);
            log.info("开始【京东账号{}】{}",i,jdCkEntity.getRemark());
            if (jdCkEntity.getStatus()==1){
                //账号已失效
                log.info("【京东账号{}】{}已失效，跳过",i,jdCkEntity.getRemark());
                continue;
            }
            cookie = jdCkEntity.getCk();
            ADID = IdUtil.randomUUID().toUpperCase();
            UUID = (IdUtil.simpleUUID() + IdUtil.simpleUUID()).substring(0,40);
            randomCode = RandomUtil.randomInt(1000000, 9999999);
            member();
        }
    }

    public void member(){
        String openCardActivityId = "";
        int addScore = 1;
        String lz_cookie = "";
        getFirstLZCK();
        String token = getToken();
        task("dz/common/getSimpleActInfoVo","activityId="+activityId,true,0);
        if (StringUtils.isNotEmpty(token)){
            JSONObject myPing = OpenCardUtil.getMyPing(UUID, ADID, activityUrl, cookie, activityShopId, token);
            secretPin = myPing.getString("secretPin");
            cookie = myPing.getString("cookie");
            pin = myPing.getString("pin");
            if (StringUtils.isNotEmpty(secretPin)){
                log.info("去助力 -> {}",ownCode);
                task("common/accessLogWithAD","venderId="+activityShopId+"&code=99&pin="+URLEncoder.encode(secretPin)+"&activityId="+activityId+"&pageUrl="+activityUrl+"&subType=app&adSource=",true,0);
                // task("wxActionCommon/getUserInfo", "pin="+URLEncoder.encode(secretPin), 1);
                if (index == 1){
                    task("joinCommon/activityContent", "activityId="+activityId+"&pin="+URLEncoder.encode(secretPin)+"&pinImg=&nick="+URLEncoder.encode(pin)+"&cjyxPin=&cjhyPin=&shareUuid="+URLEncoder.encode(authorCode), false, 1);
                }else {
                    task("joinCommon/activityContent", "activityId="+activityId+"&pin="+URLEncoder.encode(secretPin)+"&pinImg=&nick="+URLEncoder.encode(pin)+"&cjyxPin=&cjhyPin=&shareUuid="+URLEncoder.encode(authorCode), false, 0);
                }
                log.info("关注店铺");
                task("joinCommon/doTask", "activityId="+activityId+"&uuid="+actorUuid+"&pin="+URLEncoder.encode(secretPin)+"&taskType=20&taskValue=",false,0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                task("joinCommon/doTask", "activityId="+activityId+"&uuid="+actorUuid+"&pin="+URLEncoder.encode(secretPin)+"&taskType=23&taskValue=",false,0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                task("joinCommon/taskInfo", "pin="+URLEncoder.encode(secretPin)+"&activityId="+activityId,false,0);
                log.info("加入店铺会员");
                if (openCardList == null || openCardList.size() == 0){
                    for (JSONObject jsonObject : openCardList) {
                        log.info(">>> 去加入{} {}",jsonObject.getString("name"),jsonObject.getString("value"));
                        if ("0".equals(jsonObject.getString("status"))){
                            log.info(">>> 已经是会员");
                            continue;
                        }
                        JSONObject body = new JSONObject();
                        body.put("venderId",jsonObject.getString("value"));
                        body.put("channel","401");
                        getShopOpenCardInfo(body.toJSONString(),jsonObject.getString("value"));
                        body.put("bindByVerifyCodeFlag",1);
                        body.put("registerExtend",new JSONObject());
                        body.put("writeChildFlag","0");
                        body.put("activityId",openCardActivityId);
                        bindWithVender(body.toJSONString(),jsonObject.getString("value"));
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                task("joinCommon/taskInfo", "pin="+URLEncoder.encode(secretPin)+"&activityId="+activityId,false,0);
                task("joinCommon/activityContent", "activityId="+activityId+"&pin="+URLEncoder.encode(secretPin)+"&pinImg=&nick="+URLEncoder.encode(pin)+"&cjyxPin=&cjhyPin=&shareUuid="+authorCode, false, 1);
                task("joinCommon/assist", "activityId="+activityId+"&pin="+URLEncoder.encode(secretPin)+"&uuid="+actorUuid+"&shareUuid="+authorCode,false,0);
                log.info("抽奖 -> ");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                task("joinCommon/startDraw", "activityId="+activityId+"&uuid="+actorUuid+"&pin="+URLEncoder.encode(secretPin),false,0);
            }
        }
    }

    public void getFirstLZCK(){
        HttpResponse execute = HttpRequest.get(activityUrl)
                .header("user-agent", GetUserAgentUtil.getUserAgent())
                .execute();
        String result = execute.body();
        if (StringUtils.isEmpty(result) || result.contains("403")){
            throw new RuntimeException("调用京东接口失败，可能ip已黑，请尝试更换ip再试");
        }
        Map<String, List<String>> headers = execute.headers();
        cookie = cookie + headers.get("set-cookie").get(0).split(";")[0];
    }

    public String getToken(){
        String result = HttpRequest.post("https://api.m.jd.com/client.action?functionId=isvObfuscator")
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .header("Cookie", cookie)
                .header("User-Agent", "JD4iPhone/167650 (iPhone; iOS 13.7; Scale/3.00)")
                .header("Accept-Language", "zh-Hans-CN;q=1")
                .header("Accept-Encoding", "gzip, deflate, br")
                .body("body=%7B%22url%22%3A%20%22https%3A//lzkj-isv.isvjcloud.com%22%2C%20%22id%22%3A%20%22%22%7D&uuid=hjudwgohxzVu96krv&client=apple&clientVersion=9.4.0&st=1620476162000&sv=111&sign=f9d1b7e3b943b6a136d54fe4f892af05")
                .execute().body();
        if (StringUtils.isEmpty(result) || result.contains("403")){
            throw new RuntimeException("调用京东接口失败，可能ip已黑，请尝试更换ip再试");
        }else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if ("0".contains(jsonObject.getString("code"))){
                return jsonObject.getString("token");
            }else {
                throw new RuntimeException("调用京东接口失败");
            }
        }
    }

    public void task(String function_id,String body,boolean isCommon,Integer own){
        String result = HttpRequest.post(isCommon ? "https://lzdz1-isv.isvjcloud.com/" + function_id : "https://lzdz1-isv.isvjcloud.com/dingzhi/" + function_id)
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
                .header("Cookie", cookie)
                .body(body)
                .execute().body();
        if (StringUtils.isEmpty(result) || result.contains("403")){
            throw new RuntimeException("调用京东接口失败，可能ip已黑，请尝试更换ip再试");
        }else {
            log.info("task调用接口结果：{}",result);
            JSONObject data = JSONObject.parseObject(result);
            if ("0".contains(data.getString("code"))){
                switch (function_id){
                    case "dz/common/getSimpleActInfoVo":
                        jdActivityId = data.getJSONObject("data").getString("jdActivityId");
                        venderId = data.getJSONObject("data").getString("venderId");
                        activityType = data.getJSONObject("data").getString("activityType");
                        break;
                    case "wxActionCommon/getUserInfo":
                        break;
                    case "joinCommon/activityContent":
                        if (!data.getJSONObject("data").getBoolean("hasEnd")) {
                            log.info("开启【"+data.getJSONObject("data").getString("activityName")+"】活动");
                            log.info("-------------------");
                            if (index == 1) {
                                //车头
                                ownCode = data.getJSONObject("data").getJSONObject("actorInfo").getString("uuid");
                                authorCode = ownCode;
                                log.info("车头的助力码：{}",ownCode);
                            }
                            actorUuid = data.getJSONObject("data").getJSONObject("actorInfo").getString("uuid");
                        } else {
                            log.info("活动已经结束");
                        }
                        break;
                    case "joinCommon/taskInfo":
                        //todo
//                        openCardList = data.data['1']['settingInfo'];
                        openCardStatus = data.getJSONObject("data");
                        break;
                    case "joinCommon/startDraw":
                        if (StringUtils.isNotEmpty(data.getString("data"))) {
                            addScore = data.getJSONObject("data").getString("addScore");
                        }
                        break;
                    case "linkgame/sign":
                        break;
                    case "opencard/addCart":
                        break;
                    case "linkgame/sendAllCoupon":
                        break;
                    case "interaction/write/writePersonInfo":
                        break;
                    case "linkgame/draw":
                        break;
                    case "linkgame/draw/record":
                        break;
                    case "joinCommon/assist/status":
                        break;
                    case "joinCommon/assist":
                        break;
                    case "opencard/help/list":
                        break;
                    default:
                        break;
                }
            }else {
                throw new RuntimeException("调用京东接口失败");
            }
        }
    }

    public void getShopOpenCardInfo(String body,String venderId){
        String result = HttpRequest.get("https://api.m.jd.com/client.action?appid=jd_shop_member&functionId=getShopOpenCardInfo&body=" + URLEncoder.encode(body) + "&client=H5&clientVersion=9.2.0&uuid=88888")
                .header("Host", "api.m.jd.com")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .header("Cookie", cookie)
                .header("User-Agent", "jdapp;iPhone;9.5.4;13.6;" + UUID + ";network/wifi;ADID/" + ADID + ";model/iPhone10,3;addressid/0;appBuild/167668;jdSupportDarkMode/0;Mozilla/5.0 (iPhone; CPU iPhone OS 13_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148;supportJDSHWK/1")
                .header("Accept-Language", "zh-cn")
                .header("Referer", "https://shopmember.m.jd.com/shopcard/?venderId=" + venderId + "}&channel=801&returnUrl=" + activityUrl)
                .header("Accept-Encoding", "gzip, deflate, br")
                .execute().body();
        if (StringUtils.isEmpty(result) || result.contains("403")){
            throw new RuntimeException("调用京东接口失败，可能ip已黑，请尝试更换ip再试");
        }else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            Boolean success = jsonObject.getBoolean("success");
            if (success){
                if (jsonObject.getJSONObject("result").getJSONArray("interestsRuleList") != null){
                    List<JSONObject> jsonObjects = jsonObject.getJSONObject("result").getJSONArray("interestsRuleList").toJavaList(JSONObject.class);
                    openCardActivityId = jsonObjects.get(0).getJSONObject("interestsInfo").getString("activityId");
                }
            }
        }
    }

    public String geth5st(String functionId,String body) {
        JSONObject body2 = new JSONObject();
        body2.put("appid", "jd_shop_member");
        body2.put("functionId", functionId);
        body2.put("body", body);
        body2.put("clientVersion", "9.2.0");
        body2.put("client", "H5");
        body2.put("activityId", activityId);
        JSONObject data = new JSONObject();
        data.put("appId","8adfb");
        data.put("body","body2");
        data.put("callbackAll",true);

        String result = HttpRequest.post("https://cdn.nz.lu/geth5st")
                .header("Host", "jdsign.cf")
                .header("Content-Type", "application/json")
                .body(data.toJSONString())
                .timeout(30000)
                .execute().body();
        return result;
    }

    public void bindWithVender(String body,String venderId){
        String h5st = geth5st("bindWithVender",body);
        String result = HttpRequest.get("https://api.m.jd.com/client.action?" + h5st)
                .header("Host", "api.m.jd.com")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .header("Cookie", cookie)
                .header("User-Agent", "jdapp;iPhone;9.5.4;13.6;" + UUID + ";network/wifi;ADID/" + ADID + ";model/iPhone10,3;addressid/0;appBuild/167668;jdSupportDarkMode/0;Mozilla/5.0 (iPhone; CPU iPhone OS 13_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148;supportJDSHWK/1")
                .header("Accept-Language", "zh-cn")
                .header("Referer", "https://shopmember.m.jd.com/shopcard/?venderId=" + venderId + "&channel=401&returnUrl=" + URLEncoder.encode(activityUrl))
                .header("Accept-Encoding", "gzip, deflate, br")
                .execute().body();
        if (StringUtils.isEmpty(result) || result.contains("403")){
            throw new RuntimeException("调用京东接口失败，可能ip已黑，请尝试更换ip再试");
        }else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            Boolean success = jsonObject.getBoolean("success");
            if (success){
                bindWithVendermessage = jsonObject.getString("message");
            }
        }
    }
}