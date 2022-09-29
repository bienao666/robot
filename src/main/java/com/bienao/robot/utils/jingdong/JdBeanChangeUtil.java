package com.bienao.robot.utils.jingdong;

import cn.hutool.core.date.DateTime;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

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
    private Float balance = 0.00F;
    private Float expiredBalance = 0.00F;
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
     * @param jdCk
     */
    public String getJdBeanChange(JdCkEntity jdCk) {
        cookie = jdCk.getCk();
        overdue = "【挂机天数】" + DateUtil.between(DateUtil.parseDate(jdCk.getCreatedTime()), DateUtil.date(), DateUnit.DAY) + "天";
        userName = URLDecoder.decode(jdCk.getPtPin(), CharsetUtil.defaultCharset());
        try {
            TotalBean();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TotalBean2();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //汪汪乐园
        try {
            getJoyBaseInfo("", "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //京东赚赚
        try {
            getJdZZ();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //京东秒杀
        try {
            getMs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            jdfruitRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //东东农场
        try {
            getjdfruit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //极速金币
//        cash();
        //
        try {
            requestAlgo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
//        JxmcGetRequest();
        try {
            bean();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            redPacket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return showMsg();
    }

    public String showMsg() {
        String ReturnMessage = "【京东账号】" + (StringUtils.isEmpty(nickName) ? userName : nickName) + "\n";
        if (StringUtils.isNotEmpty(levelName) || StringUtils.isNotEmpty(JingXiang)){
            ReturnMessage += "【账号信息】";
        }
        if (StringUtils.isNotEmpty(levelName)){
            if (levelName.length()>2){
                levelName = levelName.substring(0,2);
            }
            if (isPlusVip==1){
                ReturnMessage += levelName + "Plus,";
            }else {
                ReturnMessage += levelName + "会员,";
            }
        }
        if (StringUtils.isNotEmpty(JingXiang)){
            ReturnMessage += JingXiang;
        }
        ReturnMessage += "\n【今日京豆】收"+todayIncomeBean+"豆";
        if (todayOutcomeBean != 0) {
            ReturnMessage += ",支"+todayOutcomeBean+"豆";
        }
        ReturnMessage += "\n【昨日京豆】收"+incomeBean+"豆";
        if (expenseBean != 0) {
            ReturnMessage += ",支"+expenseBean+"豆";
        }
        ReturnMessage += "\n【当前京豆】"+beanCount+"豆(≈"+beanCount/100+"元)\n";

        /*if (JDEggcnt == 0) {
            ReturnMessage += "【京喜牧场】未开通或提示火爆.\n";
        } else {
            ReturnMessage += "【京喜牧场】"+JDEggcnt+"枚鸡蛋\n";
        }*/

        if (JDtotalcash != 0) {
            ReturnMessage += "【极速金币】"+JDtotalcash+"币(≈"+JDtotalcash / 10000+"元)\n";
        }
        if (JdzzNum != 0) {
            ReturnMessage += "【京东赚赚】"+JdzzNum+"币(≈$"+JdzzNum / 10000+"元)\n";
        }
        if (JdMsScore != 0) {
            ReturnMessage += "【京东秒杀】"+JdMsScore+"币(≈"+JdMsScore / 1000+"元)\n";
        }

        if (joylevel != 0|| jdCash != 0) {
            ReturnMessage += "【其他信息】";
            if (joylevel != 0) {
                ReturnMessage += "汪汪:"+joylevel+"级";
                if (jdCash != 0) {
                    ReturnMessage += ",";
                }
            }
            if (jdCash != 0) {
                ReturnMessage += "领现金:"+jdCash+"元";
            }
            ReturnMessage += "\n";
        }
//
        if (StringUtils.isNotEmpty(JdFarmProdName)) {
            if (JdtreeEnergy != 0) {
                if (treeState == 2 || treeState == 3) {
                    ReturnMessage += "【东东农场】"+JdFarmProdName+" 可以兑换了!\n";
                    TempBaipiao += "【东东农场】"+JdFarmProdName+" 可以兑换了!\n";
                } else {
                    if (JdwaterD!=0) {
                        ReturnMessage += "【东东农场】"+JdFarmProdName+"("+JdtreeEnergy * 100 / JdtreeTotalEnergy +"%,"+JdwaterD+"天)\n";
                    } else {
                        ReturnMessage += "【东东农场】"+JdFarmProdName+"("+JdtreeEnergy * 100/ JdtreeTotalEnergy +"%\n";
                    }
                }
            } else {
                if (treeState == 0) {
                    TempBaipiao += "【东东农场】水果领取后未重新种植!\n";
                } else if (treeState == 1) {
                    ReturnMessage += "【东东农场】"+JdFarmProdName+"种植中...\n";
                } else {
                    TempBaipiao += "【东东农场】状态异常!\n";
                }
            }
        }
//        if ($.jxFactoryInfo) {
//            ReturnMessage += `【京喜工厂】${$.jxFactoryInfo}\n`
//        }
//        if ($.jxFactoryReceive) {
//            allReceiveMessage += `【账号${IndexAll} ${$.nickName || $.UserName}】${$.jxFactoryReceive} (京喜工厂)\n`;
//            TempBaipiao += `【京喜工厂】${$.jxFactoryReceive} 可以兑换了!\n`;
//        }
        JSONObject response = PetRequest("energyCollect");
        JSONObject initPetTownRes = PetRequest("initPetTown");
        if (initPetTownRes.getInteger("code") == 0 && initPetTownRes.getInteger("resultCode") == 0 && "success".equals(initPetTownRes.getString("message"))) {
            JSONObject petInfo = initPetTownRes.getJSONObject("result");
            if (petInfo.getInteger("userStatus") == 0) {
                ReturnMessage += "【东东萌宠】活动未开启!\n";
            } else if (petInfo.getInteger("petStatus") == 5) {
                ReturnMessage += "【东东萌宠】"+petInfo.getJSONObject("goodsInfo").getString("goodsName")+"已可领取!\n";
                TempBaipiao += "【东东萌宠】"+petInfo.getJSONObject("goodsInfo").getString("goodsName")+"已可领取!\n";
            } else if (petInfo.getInteger("petStatus") == 6) {
                TempBaipiao += "【东东萌宠】未选择物品! \n";
            } else if (response.getInteger("resultCode") == 0) {
                ReturnMessage += "【东东萌宠】"+petInfo.getJSONObject("goodsInfo").getString("goodsName");
                ReturnMessage += "("+response.getJSONObject("result").getDouble("medalPercent")+"%,"+response.getJSONObject("result").getDouble("medalNum")+"/"+(response.getJSONObject("result").getDouble("medalNum") + response.getJSONObject("result").getDouble("needCollectMedalNum")+"块)\n");
            } else if (petInfo.getJSONObject("goodsInfo")==null) {
                ReturnMessage += "【东东萌宠】暂未选购新的商品!\n";
                TempBaipiao += "【东东萌宠】暂未选购新的商品!\n";
            }
        }
        ReturnMessage += overdue+"\n";
        ReturnMessage += message;
        ReturnMessage += "\n活动攻略:" + "\n";
        ReturnMessage += "【京东赚赚】微信->京东赚赚小程序->底部赚好礼->提现无门槛红包(京东使用)\n";
        ReturnMessage += "【东东农场】京东->我的->东东农场,完成是京东红包,可以用于京东app的任意商品\n";
        ReturnMessage += "【东东萌宠】京东->我的->东东萌宠,完成是京东红包,可以用于京东app的任意商品\n";
//        ReturnMessage += "【极速金币】京东极速版->我的->金币(极速版使用)\n";
        ReturnMessage += "【京东秒杀】京东->中间频道往右划找到京东秒杀->中间点立即签到->兑换无门槛红包(京东使用)\n";
//        ReturnMessage += "【领现金】京东->我的->东东萌宠->领现金(微信提现+京东红包)\n";
//        ReturnMessage += "【京喜工厂】京喜->我的->京喜工厂,完成是商品红包,用于购买指定商品(不兑换会过期)\n";
//        ReturnMessage += "【京东金融】京东金融app->我的->养猪猪,完成是白条支付券,支付方式选白条支付时立减.\n";
        ReturnMessage += "【其他】京喜红包只能在京喜使用,其他同理";
        return ReturnMessage;
    }

    public JSONObject PetRequest(String function_id){
        JSONObject body = new JSONObject();
        body.put("version",2);
        body.put("channel","app");
        String resStr = HttpRequest.post(JDAPIHOST + "?functionId=" + function_id)
                .header("Cookie", cookie)
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("body=" + EscapeUtil.escape(body.toJSONString()) + "&appid=wh5&loginWQBiz=pet-town&clientVersion=9.0.4")
                .timeout(3000).execute().body();
        if (StringUtils.isNotEmpty(resStr)){
            return JSONObject.parseObject(resStr);
        }else {
            return null;
        }
    }

    public void redPacket() {
        try {
            String result = HttpRequest.get("https://m.jingxi.com/user/info/QueryUserRedEnvelopesV2?type=1&orgFlag=JD_PinGou_New&page=1&cashRedType=1&redBalanceFlag=1&channel=1&_=" + System.currentTimeMillis() + "&sceneval=2&g_login_type=1&g_ty=ls")
                    .header("Host", "m.jingxi.com")
                    .header("Accept", "*/*")
                    .header("Connection", "keep-alive")
                    .header("Accept-Language", "zh-cn")
                    .header("Referer", "https://st.jingxi.com/my/redpacket.shtml?newPg=App&jxsid=16156262265849285961")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Cookie", cookie)
                    .header("User-Agent", GetUserAgentUtil.getUserAgent())
                    .timeout(3000)
                    .execute().body();
            if (StringUtils.isNotEmpty(result)) {
                JSONObject res = JSONObject.parseObject(result);
                String redListStr = res.getJSONObject("data").getJSONObject("useRedInfo").getString("redList");
                if (StringUtils.isNotEmpty(redListStr)) {
                    Float jxRed = 0.00F;
                    Float jsRed = 0.00F;
                    Float jdRed = 0.00F;
                    Float jdhRed = 0.00F;
                    Float jxRedExpire = 0.00F;
                    Float jsRedExpire = 0.00F;
                    Float jdRedExpire = 0.00F;
                    Float jdhRedExpire = 0.00F;
                    long t = DateUtil.tomorrow().getTime() / 1000;
                    List<JSONObject> redList = JSONArray.parseArray(redListStr, JSONObject.class);
                    for (JSONObject red : redList) {
                        String orgLimitStr = red.getString("orgLimitStr");
                        if (StringUtils.isNotEmpty(orgLimitStr) && orgLimitStr.contains("京喜")) {
                            jxRed += Float.parseFloat(red.getString("balance"));
                            if (red.getLong("endTime") <= t) {
                                jxRedExpire += Float.parseFloat(red.getString("balance"));
                            }
                        } else if (red.getString("activityName").contains("极速版")) {
                            jsRed += Float.parseFloat(red.getString("balance"));
                            if (red.getLong("endTime") <= t) {
                                jsRedExpire += Float.parseFloat(red.getString("balance"));
                            }
                        } else if (StringUtils.isNotEmpty(red.getString("orgLimitStr")) && red.getString("orgLimitStr").contains("京东健康")) {
                            jdhRed += Float.parseFloat(red.getString("balance"));
                            if (red.getLong("endTime") <= t) {
                                jdhRedExpire += Float.parseFloat(red.getString("balance"));
                            }
                        } else {
                            jdRed += Float.parseFloat(red.getString("balance"));
                            if (red.getLong("endTime") <= t) {
                                jdRedExpire += Float.parseFloat(red.getString("balance"));
                            }
                        }
                    }
                    balance = res.getJSONObject("data").getFloat("balance");
                    expiredBalance = (jxRedExpire + jsRedExpire + jdRedExpire);
                    message += "【红包总额】" + balance + "(总过期" + String.format("%.2f", expiredBalance) + ")元";
                    if (jxRed > 0) {
                        message += "\n【京喜红包】" + String.format("%.2f", jxRed) + "(将过期" + String.format("%.2f", jxRedExpire) + ")元";
                    }
                    if (jsRed > 0) {
                        message += "\n【极速红包】" + String.format("%.2f", jsRed) + "(将过期" + String.format("%.2f", jsRedExpire) + ")元";
                    }
                    if (jdRed > 0) {
                        message += "\n【京东红包】" + String.format("%.2f", jdRed) + "(将过期" + String.format("%.2f", jdRedExpire) + ")元";
                    }
                    if (jdhRed > 0) {
                        message += "\n【健康红包】" + String.format("%.2f", jdhRed) + "(将过期" + String.format("%.2f", jdhRedExpire) + ")元";
                    }
                }
            }
        } catch (HttpException e) {
            log.error("TotalBean方法异常：" + e.getMessage());
        }
    }

    public void bean() {
        //昨天
        DateTime date = DateUtil.offsetDay(DateUtil.date(), -1);
        Long begin = DateUtil.beginOfDay(date).getTime();
        Long end = DateUtil.endOfDay(date).getTime();
        int pageSize = 50;
        int page = 1;
        int t = 0;
        do {
            JSONObject jingBeanBalanceDetail = getJingBeanBalanceDetail(cookie, page, pageSize);
            if (jingBeanBalanceDetail != null) {
                if ("0".equals(jingBeanBalanceDetail.getString("code"))) {
                    page++;
                    List<JSONObject> detailList = jingBeanBalanceDetail.getJSONArray("detailList").toJavaList(JSONObject.class);
                    for (JSONObject detail : detailList) {
                        Long time = DateUtil.parse(detail.getString("date")).getTime();
                        if (time >= end) {
                            //今天
                            String eventMassage = detail.getString("eventMassage");
                            if (!eventMassage.contains("退还") && !eventMassage.contains("物流") && !eventMassage.contains("扣赠")) {
                                int amount = Integer.parseInt(detail.getString("amount"));
                                if (amount > 0) {
                                    todayIncomeBean += amount;
                                }
                                if (amount < 0) {
                                    todayOutcomeBean += amount;
                                }
                            }
                        }
                        if (time >= begin && time <= end) {
                            //昨天
                            String eventMassage = detail.getString("eventMassage");
                            if (!eventMassage.contains("退还") && !eventMassage.contains("物流") && !eventMassage.contains("扣赠")) {
                                int amount = Integer.parseInt(detail.getString("amount"));
                                if (amount > 0) {
                                    incomeBean += amount;
                                }
                                if (amount < 0) {
                                    expenseBean += amount;
                                }
                            }
                        }
                        if (time < begin) {
                            //前天跳出
                            t = 1;
                        }
                    }
                } else if ("3".equals(jingBeanBalanceDetail.getString("code"))) {
                    log.info("ck已过期，或者填写不规范");
                    //跳出
                    t = 1;
                } else {
                    log.info("未知情况：{}", jingBeanBalanceDetail.toJSONString());
                    t = 1;
                }
            } else {
                //跳出
                t = 1;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (t == 0);

    }

    /*public void JxmcGetRequest() {
        String myRequest = "";
        String url = "https://m.jingxi.com/jxmc/queryservice/GetHomePageInfo?channel=7&sceneid=1001&activeid=null&activekey=null&isgift=1&isquerypicksite=1&_stk=channel%2Csceneid&_ste=1";
        url = url + "&h5st=${decrypt(Date.now(), '', '', url)}&_=${Date.now() + 2}&sceneval=2&g_login_type=1&callback=jsonpCBK${String.fromCharCode(Math.floor(Math.random() * 26) + \"A\".charCodeAt(0))}&g_ty=ls";

    }*/

    public String generateFp() {
        return (RandomUtil.randomNumbers(13) + System.currentTimeMillis()).substring(0, 16);
    }

    public void requestAlgo() {
        try {
            JSONObject body = new JSONObject();
            body.put("version", "1.0");
            body.put("fp", generateFp());
            body.put("appId", 10028);
            body.put("timestamp", System.currentTimeMillis());
            body.put("platform", "web");
            body.put("expandParams", "");
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
                    int waterD = new BigDecimal(waterTotalT).divide(new BigDecimal(waterEveryDayT), 0, BigDecimal.ROUND_HALF_UP).intValue();
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
                        JSONObject userInfo = data.getJSONObject("userInfo");
                        if (userInfo != null) {
                            JSONObject baseInfo = userInfo.getJSONObject("baseInfo");
                            nickName = baseInfo.getString("nickname");
                            levelName = baseInfo.getString("levelName");
                            isPlusVip = userInfo.getInteger("isPlusVip");
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
                JdMsScore = res.getJSONObject("result").getJSONObject("assignment").getInteger("assignmentPoints");
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
                if (res.getInteger("code") == 400){
                    //活动火爆中
                }else {
                    JDwaterEveryDayT = res.getJSONObject("totalWaterTaskInit").getInteger("totalWaterTaskTimes");
                }
            }
        } catch (HttpException e) {
            log.error("jdfruitRequest方法异常：" + e.getMessage());
        }
    }

    public JSONObject getJingBeanBalanceDetail(String ck, int page, int pageSize) {
        String result = HttpRequest.post("https://api.m.jd.com/client.action?functionId=getJingBeanBalanceDetail")
                .body("body=%7B%22pageSize%22%3A%22" + pageSize + "%22%2C%22page%22%3A%22" + page + "%22%7D&appid=ld")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("cookie", ck)
                .timeout(10000)
                .execute().body();
        log.info("查询京豆详情结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("查询京豆详情请求失败 ‼️‼️");
            return null;
        } else if (result.contains("response status: 403")) {
            return null;
        } else {
            return JSONObject.parseObject(result);
        }
    }
}
