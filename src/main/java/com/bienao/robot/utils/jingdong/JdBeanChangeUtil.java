package com.bienao.robot.utils.jingdong;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.Charset;

@Component
@Slf4j
public class JdBeanChangeUtil {
    private String cookie = "";
    private String overdue = "";
    private String userName = "";
    private int beanCount = 0;
    private int incomeBean = 0;
    private int expenseBean = 0;
    private int todayIncomeBean = 0;
    private int todayOutcomeBean = 0;
    private String errorMsg = "";
    private boolean isLogin = true;
    private String nickName = "";
    private String levelName = "";
    private String message = "";
    private int balance = 0;
    private int expiredBalance = 0;
    private int JdzzNum = 0;
    private int JdMsScore = 0;
    private String JdFarmProdName = "";
    private int JdtreeEnergy = 0;
    private int JdtreeTotalEnergy = 0;
    private int treeState = 0;
    private int JdwaterTotalT = 0;
    private int JdwaterD = 0;
    private int JDwaterEveryDayT = 0;
    private int JDtotalcash = 0;
    private int JDEggcnt = 0;
    private String Jxmctoken = "";
    private String DdFactoryReceive = "";
    private String jxFactoryInfo = "";
    private String jxFactoryReceive = "";
    private int jdCash = 0;
    private int isPlusVip = 0;
    private String JingXiang = "";
    //月收入
    private int allincomeBean = 0;
    //月支出
    private int allexpenseBean = 0;
    private int joylevel = 0;
    private String TempBaipiao = "";
    private String enCryptMethodJD = "";

    @Autowired
    private JdCkMapper jdCkMapper;

    @Value("${JD_API_HOST}")
    private String JDAPIHOST;

    /**
     * 查询京东资产详情
     *
     * @param ptPin
     */
    public void getJdBeanChange(String ptPin) {
        JdCkEntity jdCkQuery = new JdCkEntity();
        jdCkQuery.setPtPin(ptPin);
        JdCkEntity jdCk = jdCkMapper.queryCk(jdCkQuery);
        cookie = jdCk.getCk();
        overdue = "【挂机天数】" + DateUtil.between(DateUtil.parseDate(jdCk.getCreatedTime()), DateUtil.date(), DateUnit.DAY) + "天";
        userName = URLDecoder.decode(ptPin, CharsetUtil.defaultCharset());
        TotalBean();
        TotalBean2();
        //汪汪乐园
        getJoyBaseInfo("", "", "");
        //京东赚赚
        getJdZZ();
        //京东秒杀
        getMs();
        jdfruitRequest();
        //东东农场
        getjdfruit();
        //极速金币
//        cash();
        //
        requestAlgo();
        //
        JxmcGetRequest();
    }

    public void JxmcGetRequest() {

    }

    public String generateFp(){
        return (RandomUtil.randomNumbers(13)+System.currentTimeMillis()).substring(0,16);
    }

    public void requestAlgo() {
        try {
            JSONObject body = new JSONObject();
            body.put("version","1.0");
            body.put("fp",generateFp());
            body.put("appId",10028);
            body.put("timestamp",System.currentTimeMillis());
            body.put("platform","web");
            body.put("expandParams","");
            String resultStr = HttpRequest.post("https://cactus.jd.com/request_algo?g_ty=ajax")
                    .header("Authority", "cactus.jd.com")
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .header("Accept", "application/json")
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .header("Origin", "https://st.jingxi.com")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Referer", "https://st.jingxi.com/")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,en;q=0.7")
                    .body(URLEncodeUtil.encode(body.toJSONString()))
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(resultStr)) {
                JSONObject res = JSONObject.parseObject(resultStr);
                if (res.getInteger("status") == 200) {
                    JSONObject result = res.getJSONObject("data").getJSONObject("result");
                    Jxmctoken = result.getString("tk");
                    enCryptMethodJD = result.getString("algo");
                }
            }
        } catch (HttpException e) {
            log.error("getMs方法异常：" + e.getMessage());
        }
    }

    public void cash() {
        try {

        } catch (HttpException e) {
            log.error("cash方法异常：" + e.getMessage());
        }
    }

    public void getjdfruit() {
        try {
            JSONObject body = new JSONObject();
            body.put("version", 4);
            String result = HttpRequest.post(JDAPIHOST + "?functionId=initForFarm")
                    .header("accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("accept-language", "zh-CN,zh;q=0.9")
                    .header("cache-control", "no-cache")
                    .header("Cookie", cookie)
                    .header("origin", "https://home.m.jd.com")
                    .header("pragma", "no-cache")
                    .header("Referer", "https://home.m.jd.com/myJd/newhome.action")
                    .header("sec-fetch-dest", "empty")
                    .header("sec-fetch-mode", "cors")
                    .header("sec-fetch-site", "same-site")
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("body=" + URLEncodeUtil.encode(body.toJSONString()) + "&appid=wh5&clientVersion=9.1.0")
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject farmInfo = JSONObject.parseObject(result);
                JSONObject farmUserPro = farmInfo.getJSONObject("farmUserPro");
                if (farmUserPro != null) {
                    JdFarmProdName = farmUserPro.getString("name");
                    JdtreeEnergy = farmUserPro.getInteger("treeEnergy");
                    JdtreeTotalEnergy = farmUserPro.getInteger("treeTotalEnergy");
                    treeState = farmInfo.getInteger("treeState");
                    int waterEveryDayT = JDwaterEveryDayT;
                    //一共还需浇多少次水
                    int waterTotalT = (farmUserPro.getInteger("treeTotalEnergy") - farmUserPro.getInteger("treeEnergy") - farmUserPro.getInteger("totalEnergy")) / 10;
                    int waterD = new BigDecimal(waterTotalT).divide(new BigDecimal(waterEveryDayT),0,BigDecimal.ROUND_HALF_UP).intValue();
                    JdwaterTotalT = waterTotalT;
                    JdwaterD = waterD;
                }
            }
        } catch (HttpException e) {
            log.error("getjdfruit方法异常：" + e.getMessage());
        }
    }

    public void TotalBean() {
        try {
            String result = HttpRequest.get("https://me-api.jd.com/user_new/info/GetJDUserInfoUnion")
                    .header("Host", "me-api.jd.com")
                    .header("Accept", "*/*")
                    .header("Connection", "keep-alive")
                    .header("Cookie", cookie)
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .header("Accept-Language", "zh-cn")
                    .header("Referer", "https://home.m.jd.com/myJd/newhome.action?sceneval=2&ufc=&")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                if (res.getInteger("retcode") == 1001) {
                    isLogin = false;
                    return;
                }
                if (res.getInteger("retcode") == 0) {
                    JSONObject data = res.getJSONObject("data");
                    if (data != null) {
                        if (data.getJSONObject("userInfo") != null) {
                            JSONObject baseInfo = data.getJSONObject("baseInfo");
                            nickName = baseInfo.getString("nickname");
                            levelName = baseInfo.getString("levelName");
                            isPlusVip = baseInfo.getInteger("isPlusVip");
                        }
                        if (data.getJSONObject("assetInfo") != null) {
                            JSONObject assetInfo = data.getJSONObject("assetInfo");
                            beanCount = assetInfo.getInteger("beanNum");
                        }
                    }
                }
            }
        } catch (HttpException e) {
            log.error("TotalBean方法异常：" + e.getMessage());
        }
    }

    public void TotalBean2() {
        try {
            String result = HttpRequest.post("https://wxapp.m.jd.com/kwxhome/myJd/home.json?&useGuideModule=0&bizId=&brandId=&fromType=wxapp&timestamp=" + DateUtil.date().toTimestamp())
                    .header("Cookie", cookie)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("Connection", "keep-alive")
                    .header("Accept-Encoding", "gzip,compress,br,deflate")
                    .header("Referer", "https://servicewechat.com/wxa5bf5ee667d91626/161/page-frame.html")
                    .header("Host", "wxapp.m.jd.com")
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                JSONObject userInfo = res.getJSONObject("user");
                if (userInfo == null) {
                    isLogin = false;
                    return;
                }
                String petName = userInfo.getString("petName");
                if (StringUtils.isNotEmpty(petName)) {
                    nickName = petName;
                }
                if (beanCount == 0) {
                    beanCount = userInfo.getInteger("jingBean");
                    isPlusVip = 3;
                    JingXiang = userInfo.getString("uclass");
                }
            }
        } catch (HttpException e) {
            log.error("TotalBean2方法异常：" + e.getMessage());
        }
    }

    public void getJoyBaseInfo(String taskId, String inviteType, String inviterPin) {
        try {
            JSONObject body = new JSONObject();
            body.put("taskId", taskId);
            body.put("inviteType", inviteType);
            body.put("inviterPin", inviterPin);
            body.put("linkId", "LsQNxL7iWDlXUs6cFl-AAg");
            String result = HttpRequest.post("https://api.m.jd.com/client.action?functionId=joyBaseInfo")
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("Host", "api.m.jd.com")
                    .header("Origin", "https://joypark.jd.com")
                    .header("Referer", "https://joypark.jd.com/?activityId=LsQNxL7iWDlXUs6cFl-AAg&lng=113.387899&lat=22.512678&sid=4d76080a9da10fbb31f5cd43396ed6cw&un_area=19_1657_52093_0")
                    .header("Cookie", cookie)
                    .body("body=" + EscapeUtil.escape(body.toJSONString()) + "&appid=activities_platform")
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                if (res.getBoolean("success")) {
                    joylevel = res.getJSONObject("data").getInteger("level");
                }
            }
        } catch (HttpException e) {
            log.error("getJoyBaseInfo方法异常：" + e.getMessage());
        }
    }

    public void getJdZZ() {
        try {
            JSONObject body = new JSONObject();
            URLEncoder urlEncoder = URLEncoder.createDefault();
            String result = HttpRequest.get(JDAPIHOST + "?functionId=interactTaskIndex&body=" + urlEncoder.encode(body.toJSONString(), Charset.defaultCharset()) + "&client=wh5&clientVersion=9.1.0")
                    .header("Cookie", cookie)
                    .header("Host", "api.m.jd.com")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Referer", "http://wq.jd.com/wxapp/pages/hd-interaction/index/index")
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .header("Accept-Language", "zh-cn")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                JSONObject data = res.getJSONObject("data");
                if (data != null) {
                    JdzzNum = data.getInteger("totalNum");
                }
            }
        } catch (HttpException e) {
            log.error("getJdZZ方法异常：" + e.getMessage());
        }
    }

    public void getMs() {
        try {
            JSONObject body = new JSONObject();
            String result = HttpRequest.post(JDAPIHOST)
                    .header("Cookie", cookie)
                    .header("Origin", "https://h5.m.jd.com")
                    .header("Referer", "https://h5.m.jd.com/babelDiy/Zeus/2NUvze9e1uWf4amBhe1AV6ynmSuH/index.html")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .body("functionId=homePageV2&body=" + URLEncodeUtil.encode(body.toJSONString()) + "&client=wh5&clientVersion=1.0.0&appid=SecKill2020")
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                if (res.getInteger("code") == 2041 || res.getInteger("code") == 2042) {
                    JdMsScore = res.getJSONObject("data").getJSONObject("result").getJSONObject("assignment").getInteger("assignmentPoints");
                }
            }
        } catch (HttpException e) {
            log.error("getMs方法异常：" + e.getMessage());
        }
    }

    public void jdfruitRequest() {
        try {
            JSONObject body = new JSONObject();
            body.put("version", 14);
            body.put("channel", 1);
            body.put("babelChannel", "120");
            URLEncoder urlEncoder = URLEncoder.createDefault();
            String result = HttpRequest.get(JDAPIHOST + "?functionId=taskInitForFarm&appid=wh5&body=" + urlEncoder.encode(body.toJSONString(), Charset.defaultCharset()))
                    .header("Cookie", cookie)
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                JDwaterEveryDayT = res.getInteger("totalWaterTaskTimes");
            }
        } catch (HttpException e) {
            log.error("jdfruitRequest方法异常：" + e.getMessage());
        }
    }

}
