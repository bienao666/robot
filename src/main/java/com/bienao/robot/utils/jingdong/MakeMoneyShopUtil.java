package com.bienao.robot.utils.jingdong;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 特价版大赢家
 */
@Slf4j
public class MakeMoneyShopUtil {

    public static void main(String[] args) {
        String ck = "pt_key=AAJjYG30ADAxiz95zW1VRawAPJOKekhBpQAAqfnEJoVWpzsmBVZxMgueUCz7iqrysMg-JrmUmQI;pt_pin=jd_6df829e3c988b;";
        String fn = "makemoneyshop/home";
        String body = "activeId=63526d8f5fe613a6adb48f03&_stk=activeId&_ste=1";
        String url = "https://wq.jd.com/" + fn + "?g_ty=h5&g_tk=&appCode=msc588d6d5&" + body + "&h5st=&sceneval=2&callback=__jsonp1667344808184";
        String str = HttpRequest.get(url)
                .header("Origin", "https://wq.jd.com")
                .header("Referer", "https://wqs.jd.com/sns/202210/20/make-money-shop/index.html?activeId=63526d8f5fe613a6adb48f03")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Cookie", ck)
                .execute().body();
        System.out.println(str);
        int start = str.indexOf("(");
        int end = str.lastIndexOf(")");
        String substring = str.substring(start + 1, end);
        System.out.println(substring);
        JSONObject jsonObject = JSONObject.parseObject(substring);
        System.out.println(jsonObject.toJSONString());
    }

    /**
     * 获取信息
     * @param ck ck
     * @return
     */
    public static JSONObject getInfo(String ck){
        String fn = "makemoneyshop/home";
        String body = "activeId=63526d8f5fe613a6adb48f03&_stk=activeId&_ste=1";
        JSONObject data = get(fn, body, ck);
        log.info("赚钱大赢家获取个人信息：{}",data.toJSONString());
        if (data == null){
            return null;
        }
        JSONObject result = new JSONObject();
        Integer code = data.getInteger("code");
        result.put("code",code);
        if (0 == code){
            //是否火爆
            result.put("hot",0);
            //助力码
            String sId = data.getJSONObject("data").getString("shareId");
            result.put("sId",sId);
            //当前营业金
            Double canUseCoinAmount = data.getJSONObject("data").getDouble("canUseCoinAmount");
            result.put("canUseCoinAmount",canUseCoinAmount);
        }else {
            //是否火爆
            result.put("hot",1);
        }
        return result;
    }

    /**
     * 获取任务
     * @param ck ck
     * @return
     */
    public static List<JSONObject> getTask(String ck){
        String fn = "newtasksys/newtasksys_front/GetUserTaskStatusList";
        String body = "__t="+DateUtil.date().getTime()+"&source=makemoneyshop&bizCode=makemoneyshop";
        JSONObject data = get(fn, body, ck);
        log.info("赚钱大赢家获取任务：{}",data.toJSONString());
        if (data == null){
            return null;
        }
        JSONObject result = new JSONObject();
        Integer ret = data.getInteger("ret");
        if (0 == ret){
            String userTaskStatusListStr = data.getJSONObject("data").getString("userTaskStatusList");
            List<JSONObject> userTaskStatusList = JSONObject.parseArray(userTaskStatusListStr, JSONObject.class);
            return userTaskStatusList;
        }
        return null;
    }

    /**
     * 奖励
     * @param ck ck
     * @return
     */
    public static String award(String ck,Integer taskId){
        String fn = "newtasksys/newtasksys_front/Award";
        String body = "__t="+DateUtil.date().getTime()+"&source=makemoneyshop&taskId="+taskId+"&bizCode=makemoneyshop";
        JSONObject data = get(fn, body, ck);
        log.info("赚钱大赢家奖励：{}",data.toJSONString());
        if (data == null){
            return "";
        }
        Integer ret = data.getInteger("ret");
        if (0 == ret){
            Integer prizeInfo = data.getJSONObject("data").getInteger("prizeInfo");
            Double money = new BigDecimal(prizeInfo).divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP).doubleValue();
            return "获得营业金："+money+"元";
        }
        return "result";
    }


    /**
     * 助力
     * @param shareid 被助力码
     * @param ck ck
     * @return
     */
    public static JSONObject help(String shareid, String ck){
        String fn = "makemoneyshop/guesthelp";
        String body = "activeId=63526d8f5fe613a6adb48f03&shareId=" + shareid + "&_stk=activeId,shareId&_ste=1";
        JSONObject data = get(fn, body, ck);
        log.info("赚钱大赢家助力：{}",data.toJSONString());
        if (data == null){
            return null;
        }
        JSONObject result = new JSONObject();
        Integer code = data.getInteger("code");
        result.put("code",code);
        if (0 == code){
            //助力成功
            result.put("nohelp",1);
        }else if ("已助力".equals(data.getString("msg"))){
            //已助力过TA
            result.put("nohelp",1);
        }else if (1006 == code){
            //不能为自己助力
            result.put("nohelp",2);
        }else if (1008 == code){
            //今日无助力次数了！
            result.put("nohelp",0);
        }else {
            //其他情况
            result.put("nohelp",3);
            result.put("msg",data.getString("msg"));
            log.info(data.getString("msg"));
        }
        return result;
    }

    private static JSONObject get(String fn,String body,String ck){
        String url = "https://wq.jd.com/" + fn + "?g_ty=h5&g_tk=&appCode=msc588d6d5&" + body + "&h5st=&sceneval=2&callback=__jsonp1667344808184";
        String str =  HttpRequest.get(url)
                .header("Origin", "https://wq.jd.com")
                .header("Referer", "https://wqs.jd.com/sns/202210/20/make-money-shop/index.html?activeId=63526d8f5fe613a6adb48f03")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Cookie", ck)
                .execute().body();
        if (StringUtils.isEmpty(str)){
            return null;
        }
        int start = str.indexOf("(");
        int end = str.lastIndexOf(")");
        String substring = str.substring(start + 1, end);
        return JSONObject.parseObject(substring);
    }
}
