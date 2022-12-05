package com.bienao.robot.utils.jingdong;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 特价版大赢家
 */
@Slf4j
public class MakeMoneyShopUtil {

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
            result.put("code",0);
            //不火爆
            result.put("hot",0);
            //助力码
            String sId = data.getJSONObject("data").getString("shareId");
            result.put("sId",sId);
            //当前营业金
            Double canUseCoinAmount = data.getJSONObject("data").getDouble("canUseCoinAmount");
            result.put("canUseCoinAmount",canUseCoinAmount);
        }else if (13 == code){
            result.put("code",13);
        }else {
            result.put("code",1);
            //火爆
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
    public static JSONObject award(String ck,Integer taskId){
        String fn = "newtasksys/newtasksys_front/Award";
        String body = "__t="+DateUtil.date().getTime()+"&source=makemoneyshop&taskId="+taskId+"&bizCode=makemoneyshop";
        JSONObject data = get(fn, body, ck);
        log.info("赚钱大赢家奖励：{}",data.toJSONString());
        if (data == null){
            return data;
        }
        Integer ret = data.getInteger("ret");
        if (0 == ret){
            Integer prizeInfo = data.getJSONObject("data").getInteger("prizeInfo");
            Double money = new BigDecimal(prizeInfo).divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP).doubleValue();
            JSONObject res = new JSONObject();
            res.put("code",0);
            res.put("money",money);
            return res;
        }
        return null;
    }
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        in = new Scanner(System.in);
        int q = in.nextInt();
        //0 黑 1白
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(0);
        }
        for (int i = 0; i < q; i++) {
            in = new Scanner(System.in);
            int L = in.nextInt();
            in = new Scanner(System.in);
            int R = in.nextInt();
            for (int j = 0; j < list.size(); j++) {
                if (j >= L && j <=R){
                    list.set(j,list.get(j)==0?1:0);
                }
            }
        }

        long count = list.stream().filter(i -> i == 0).count();
        System.out.println(count);


    }


    /**
     * 助力
     * @param shareid 被助力码
     * @param ck ck
     * @return
     */
    public static JSONObject help(String shareid, String ck){
        String fn = "makemoneyshop/guesthelp";
        JSONObject body = new JSONObject();
        body.put("activeId","63526d8f5fe613a6adb48f03");
        body.put("shareId",shareid);
        body.put("operType",1);
        JSONObject data = helpGet(body, ck);
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
        }else if (147 == code){
            //活动火爆
            result.put("nohelp",4);
        }else if (1009 == code){
            //助力任务已完成
            result.put("nohelp",5);
        }else if (1002 == code){
            //shareid错误
            result.put("code",1002);
            result.put("msg",data.getString("msg"));
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
                .header("Referer", "https://wqs.jd.com/")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Cookie", ck)
                .execute().body();
        if (StringUtils.isEmpty(str)){
            return null;
        }
        str = str.replace("try{","").replace(";} catch (e) {}","");
        int start = str.indexOf("(");
        int end = str.lastIndexOf(")");
        String substring = str.substring(start + 1, end);
        return JSONObject.parseObject(substring);
//        return JSONObject.parseObject(str);
    }

    private static JSONObject helpGet(JSONObject body,String ck){
        URLEncoder urlEncoder = new URLEncoder();
        String url = "https://api.m.jd.com/api?g_ty=h5&g_tk=&appCode=msc588d6d5&body=" + urlEncoder.encode(body.toJSONString(), Charset.defaultCharset()) + "&appid=jdlt_h5&client=jxh5&functionId=makemoneyshop_guesthelp&clientVersion=1.2.5&h5st=&loginType=2&sceneval=2";
        String str =  HttpRequest.get(url)
                .header("Origin", "https://wq.jd.com")
                .header("Referer", "https://wqs.jd.com/")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Cookie", ck)
                .execute().body();
        if (StringUtils.isEmpty(str)){
            return null;
        }
        /*str = str.replace("try{","").replace(";} catch (e) {}","");
        int start = str.indexOf("(");
        int end = str.lastIndexOf(")");
        String substring = str.substring(start + 1, end);
        return JSONObject.parseObject(substring);*/
        return JSONObject.parseObject(str);
    }
}
