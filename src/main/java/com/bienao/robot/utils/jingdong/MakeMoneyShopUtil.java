package com.bienao.robot.utils.jingdong;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;

/**
 * 特价版大赢家
 */
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

    }

    /**
     * 获取信息
     * @param ck ck
     * @return
     */
    public static String getInfo(String ck){
        String fn = "makemoneyshop/home";
        String body = "activeId=63526d8f5fe613a6adb48f03&_stk=activeId&_ste=1";
        return get(fn,body,ck);
    }

    /**
     * 获取任务
     * @param ck ck
     * @return
     */
    public static String getTask(String ck){
        String fn = "newtasksys/newtasksys_front/GetUserTaskStatusList";
        String body = "__t="+DateUtil.date().getTime()+"&source=makemoneyshop&bizCode=makemoneyshop";
        return get(fn,body,ck);
    }

    /**
     * 打扫
     * @param ck ck
     * @return
     */
    public static String award(String ck){
        String fn = "newtasksys/newtasksys_front/Award";
        String body = "__t="+DateUtil.date().getTime()+"&source=makemoneyshop&taskId=3532&bizCode=makemoneyshop";
        return get(fn,body,ck);
    }


    /**
     * 助力
     * @param shareid 被助力码
     * @param ck ck
     * @return
     */
    public static String help(String shareid, String ck){
        String fn = "makemoneyshop/guesthelp";
        String body = "activeId=63526d8f5fe613a6adb48f03&shareId=" + shareid + "&_stk=activeId,shareId&_ste=1";
        return get(fn,body,ck);
    }

    private static String get(String fn,String body,String ck){
        String url = "https://wq.jd.com/" + fn + "?g_ty=h5&g_tk=&appCode=msc588d6d5&" + body + "&h5st=&sceneval=2&callback=__jsonp1667344808184";
        return HttpRequest.get(url)
                .header("Origin", "https://wq.jd.com")
                .header("Referer", "https://wqs.jd.com/sns/202210/20/make-money-shop/index.html?activeId=63526d8f5fe613a6adb48f03")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Cookie", ck)
                .execute().body();
    }
}
