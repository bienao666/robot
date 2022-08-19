package com.bienao.robot.service.impl.weixin;

import cn.hutool.cache.Cache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.Festival;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.QingLongGuanLiUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import com.google.common.collect.EvictingQueue;
import com.nlf.calendar.Lunar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WxServiceImpl implements WxService {

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private QingLongGuanLiUtil qingLongGuanLiUtil;

    private Cache<String, String> redis = WXConstant.redis;

    @Async
    @Override
    public void handleMessage(JSONObject message) {
        JSONObject content = message.getJSONObject("content");
        //发送人
        String from_wxid = content.getString("from_wxid");

        //查看当前会话
        String curSession = redis.get(from_wxid + "curSession");
        if (StringUtils.isNotEmpty(curSession)) {
            if ("青龙管理".equals(curSession)) {
                qingLongGuanLiUtil.handleOperate(content);
                return;
            }

        }

        String msg = content.getString("msg");
        //微博
        if (msg.contains("微博") || msg.contains("热搜") || msg.contains("wb")) {
            handleWeiBo(content);
            return;
        }
        //举牌
        if (msg.contains("举牌")) {
            handleJuPai(content);
            return;
        }
        //买家秀
        if (msg.trim().contains("mjx") || msg.trim().contains("买家秀")) {
            handleMJX(content);
            return;
        }
        //摸鱼
        if (msg.trim().equals("my") || msg.trim().equals("摸鱼")) {
            handleMoYu(content);
            return;
        }
        //比价
        if (msg.contains("item.m.jd.com") || msg.contains("m.tb.cn")) {
            handleGoods(msg, content);
            return;
        }
        //青龙管理
        if (msg.trim().equals("青龙测试")) {
            qingLongGuanLiUtil.handleQingLong(content);
            return;
        }
        //设置微信管理员
        if (msg.contains("设置微信管理员")) {
            handleSetWXMasters(content);
            return;
        }
        //查询我的uid
        if (msg.trim().equals("我的uid") || msg.trim().equals("myuid")) {
            handleMyUid(content);
            return;
        }
        //查询群id
        if (msg.trim().equals("群号") || msg.trim().equals("groupCode")) {
            handleMyUid(content);
            return;
        }
        //查询微信管理员
        if (msg.trim().equals("微信管理员列表")) {
            handleQueryWXMasters(content);
            return;
        }

        if (NumberUtil.isInteger(msg)) {
            Integer num = Integer.valueOf(msg);
            String publicKey = redis.get("publicKey");
            if (num <= 50 && StringUtils.isNotEmpty(publicKey)) {
                handleLast(content, num, publicKey);
            }
        }
    }

    /**
     * 查询微信管理员
     */
    public void handleQueryWXMasters(JSONObject content) {
        String masters = systemParamUtil.querySystemParam("wxmasters");
        if (StringUtils.isEmpty(masters)) {
            weChatUtil.sendTextMsg("尚未设置管理员，请先按照以下命令设置,多个请用@隔开", content);
            weChatUtil.sendTextMsg("设置微信管理员 你的uid", content);
        }
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            weChatUtil.sendTextMsg(masters, content);
        }
    }

    /**
     * 查询群id
     *
     * @param content
     */
    public void handleGroupCode(JSONObject content) {
        //发送人
        String from_group = content.getString("from_group");
        weChatUtil.sendTextMsg(from_group, content);
    }


    /**
     * 查询我的uid
     *
     * @param content
     */
    public void handleMyUid(JSONObject content) {
        //发送人
        String from_wxid = content.getString("from_wxid");
        weChatUtil.sendTextMsg(from_wxid, content);
    }

    /**
     * 设置微信管理员
     *
     * @param content
     */
    public void handleSetWXMasters(JSONObject content) {
        //发送人
        String from_wxid = content.getString("from_wxid");
        String masters = systemParamUtil.querySystemParam("wxmasters");
        String newMaster = content.getString("msg").replace("设置微信管理员", "").trim();
        if (StringUtils.isEmpty(masters)) {
            //第一次直接设置
            masters = newMaster;
            SystemParam systemParam = new SystemParam();
            systemParam.setCode("wxmasters");
            systemParam.setCodeName("微信管理员");
            systemParam.setValue(masters);
            boolean flag = systemParamUtil.addSystemParam(systemParam);
            if (flag) {
                weChatUtil.sendTextMsg("设置成功", content);
            } else {
                weChatUtil.sendTextMsg("设置失败，系统异常", content);
            }
        } else {
           if (!masters.contains(newMaster)) {
                //添加新的管理员
                masters = masters + "@" + content.getString("msg").replace("设置微信管理员", "").trim();
                boolean flag = systemParamUtil.updateSystemParam("wxmasters", "微信管理员", masters);
                if (flag) {
                    weChatUtil.sendTextMsg("设置成功", content);
                } else {
                    weChatUtil.sendTextMsg("设置失败，系统异常", content);
                }
            }else {
               //已设置
               weChatUtil.sendTextMsg("设置成功", content);
           }
        }
    }

    public void handleLast(JSONObject content, Integer num, String publicKey) {
        if (publicKey.equals("微博")) {
            handleLastWeiBo(content, num);
        }

    }

    /**
     * 比价
     *
     * @param url
     */
    public void handleGoods(String url, JSONObject content) {
        String resStr = doGetGoods(url);
        if (StringUtils.isNotEmpty(resStr)) {
            JSONObject res = JSONObject.parseObject(resStr);
            String singleStr = res.getString("single");
            if (StringUtils.isNotEmpty(singleStr)) {
                JSONObject single = JSONObject.parseObject(singleStr);
                dohandleGoods(single, content);
            } else {
                //再尝试一次
                resStr = doGetGoods(url);
                if (StringUtils.isNotEmpty(resStr)) {
                    res = JSONObject.parseObject(resStr);
                    singleStr = res.getString("single");
                    if (StringUtils.isNotEmpty(singleStr)) {
                        JSONObject single = JSONObject.parseObject(singleStr);
                        dohandleGoods(single, content);
                    } else {
                        log.info("比价失败！！！");
                    }
                }
            }
        } else {
            //再尝试一次
            resStr = doGetGoods(url);
            if (StringUtils.isNotEmpty(resStr)) {
                JSONObject res = JSONObject.parseObject(resStr);
                String singleStr = res.getString("single");
                if (StringUtils.isNotEmpty(singleStr)) {
                    JSONObject single = JSONObject.parseObject(singleStr);
                    dohandleGoods(single, content);
                } else {
                    log.info("比价失败！！！");
                }
            }
        }
    }

    public void dohandleGoods(JSONObject single, JSONObject content) {
        String result = "";
        //商品名称
        String title = single.getString("title");
        result += "商品名称：" + title + "\r\n";
        //商品链接
        String goodsUrl = single.getString("url");
//            result += "商品链接：" + goodsUrl + "\r\n";
        //当前价格
        //当前价格
        String spmoney = single.getString("spmoney");
        result += "当前价格：" + spmoney + "\r\n";
        //当前价格状态
        String currentPriceStatus = single.getString("currentPriceStatus");
        result += "当前状态：" + currentPriceStatus + "\r\n";
        //趋势变动
        String changPriceRemark = single.getString("changPriceRemark");
        result += "趋势变动：" + changPriceRemark + "\r\n";
        //价格趋势
        String qushi = single.getString("qushi");
        result += "价格趋势：" + qushi + "\r\n";
        //大图
        String bigpic = single.getString("bigpic");

        //小图
        String smallpic = single.getString("smallpic");

        //最低价格
        String lowerPrice = single.getString("lowerPrice");
        result += "最低价格：" + lowerPrice + "\r\n";
        //最低日期  lowerDate -> /Date(1599753600000+0800)/
        String lowerDate = single.getString("lowerDate");
        Pattern compile = Pattern.compile("/Date\\((\\d+)\\+");
        Matcher matcher = compile.matcher(lowerDate);
        if (matcher.find()) {
            Date date = new Date(Long.valueOf(matcher.group(1)));
            lowerDate = DateUtil.format(date, "yyyy.MM.dd");
            result += "最低日期：" + lowerDate + "\r\n";
        }

        //价格趋势
        String jiagequshi = single.getString("jiagequshi");
        String[] split = jiagequshi.replace("[Date.UTC(", "").replace(")", "").replace("]", "").split(",");
        List<String> list = Arrays.asList(split);
        List<List<String>> lists = ListUtil.split(list, 5);
        EvictingQueue<String> priceChangeList = EvictingQueue.create(20);
        String price = "";
        for (List<String> object : lists) {
            String p = object.get(3);
            if (StringUtils.isEmpty(price)) {
                price = p;
                String raw = object.get(0) + "-" + object.get(1) + "-" + object.get(2) + "：" + price;
                priceChangeList.add(raw);
            } else {
                BigDecimal last = new BigDecimal(price);
                BigDecimal now = new BigDecimal(p);
                if (now.compareTo(last) != 0) {
                    price = p;
                    String raw = object.get(0) + "-" + object.get(1) + "-" + object.get(2) + "：" + price;
                    priceChangeList.add(raw);
                }
            }
        }
        if (priceChangeList.size() > 0) {
            result += "最近20次价格变动如下：\r\n";
            for (String s : priceChangeList) {
                result += s + "\r\n";
            }
        }
        weChatUtil.sendImageMsg(bigpic, content);
        weChatUtil.sendTextMsg(result, content);
    }

    /**
     * 调查询商品接口
     *
     * @param url
     * @return
     */
    public String doGetGoods(String url) {
        url = url.replaceAll("/", "%252F").replaceAll("\\?", "%253F").replaceAll("=", "%253D").replaceAll(":", "%253A").replaceAll("&", "%26");
        String body = "c_devid=2C5039AF-99D0-4800-BC36-DEB3654D202C&username=&qs=true&c_engver=1.2.35&c_devtoken=&c_devmodel=iPhone%20SE&c_contype=wifi&t=1537348981671&c_win=w_320_h_568&p_url=" + url + "&c_ostype=ios&jsoncallback=%3F&c_ctrl=w_search_trend0_f_content&methodName=getBiJiaInfo_wxsmall&c_devtype=phone&jgzspic=no&c_operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8&c_appver=2.9.0&bj=false&c_dp=2&c_osver=10.3.3";
        String resStr = HttpRequest.post("https://apapia.manmanbuy.com/ChromeWidgetServices/WidgetServices.ashx")
                .header("Host", "apapia.manmanbuy.com")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .header("Proxy-Connection", "close")
                .header("Cookie", "ASP.NET_SessionId=uwhkmhd023ce0yx22jag2e0o; jjkcpnew111=cp46144734_1171363291_2017/11/25")
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X) AppleWebKit/603.3.8 (KHTML like Gecko) Mobile/14G60 mmbWebBrowse")
                .header("Content-Length", "457")
                .header("Accept-Encoding", "gzip")
                .header("Connection", "close")
                .body(body)
                .execute().body();
        return resStr;
    }

    /**
     * 饿了么定时
     *
     * @param content
     */
    public void timeHandleELM(JSONObject content) {
        weChatUtil.sendTextMsg("到饭点啦，饿了么扫码领大额红包！！！", content);
        weChatUtil.sendImageMsg("http://i.imgtg.com/2022/08/09/AEf1U.jpg", content);
    }

    /**
     * 饿了么
     *
     * @param content
     */
    private void handleELM(JSONObject content) {
        weChatUtil.sendImageMsg("https://img2.baidu.com/it/u=2624834020,3993605127&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1660150800&t=e8768caa35dc64bcf383764c7d9ec691", content);
    }

    /**
     * 买家秀
     *
     * @param content
     */
    private void handleMJX(JSONObject content) {
        weChatUtil.sendImageMsg("http://api.uomg.com/api/rand.img3", content);
    }

    /**
     * 举牌
     *
     * @param content
     */
    private void handleJuPai(JSONObject content) {
        String msg = content.getString("msg");
        msg = msg.replace("举牌", "").replace(" ", "");
        if (StringUtils.isEmpty(msg)) {
            weChatUtil.sendTextMsg("你要举牌啥啊。。。举牌空气嘛\r\n\r\n正确命令是：举牌 要举牌的内容", content);
        } else {
            weChatUtil.sendImageMsg("http://lkaa.top/API/pai/?msg=" + URLEncoder.encode(msg), content);
        }
    }

    /**
     * 微博
     *
     * @param content
     */
    @Override
    public void handleWeiBo(JSONObject content) {
        redis.put("publicKey", "微博", DateUnit.SECOND.getMillis() * 60 * 2);
        String result = HttpRequest.get("https://weibo.com/ajax/statuses/hot_band").execute().body();
        if (StringUtils.isEmpty(result)) {
            weChatUtil.sendTextMsg("微博接口废拉，赶紧联系管理员维护！！！", content);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String bandListStr = jsonObject.getJSONObject("data").getString("band_list");
        List<JSONObject> bandLists = JSONArray.parseArray(bandListStr, JSONObject.class);
        //加入缓存
        redis.put("WbBandLists", bandListStr, DateUnit.SECOND.getMillis() * 60 * 2);
        String msg = "微博实时热搜：\r\n\r\n";
        int index = 1;
        for (JSONObject band : bandLists) {
            String realpos = band.getString("realpos");
            if (StringUtils.isNotEmpty(realpos)) {
                msg = msg + index + "." + band.getString("word") + "\r\n";
                index++;
            }
            if (index == 21) {
                break;
            }
        }
        weChatUtil.sendTextMsg(msg, content);
    }

    public void handleLastWeiBo(JSONObject content, Integer num) {
        String bandListStr = redis.get("WbBandLists");
        List<JSONObject> bandLists = JSONArray.parseArray(bandListStr, JSONObject.class);
        int index = 1;
        for (JSONObject band : bandLists) {
            String realpos = band.getString("realpos");
            if (StringUtils.isNotEmpty(realpos)) {
                if (index == num) {
                    String word = band.getString("word");
                    weChatUtil.sendTextMsg(word + "\r\n" + "https://s.weibo.com/weibo?q=%23" + URLEncoder.encode(word) + "%23", content);
                    break;
                }
                index++;
            }
        }
    }


    /**
     * 摸鱼
     *
     * @param content
     * @return
     */
    public void handleMoYu(JSONObject content) {
        // 获取当前日期
        Date now = new Date();
        int year = DateUtil.year(now);
        // 添加节日
        List<Festival> festivalList = new ArrayList<>();
        // 清明节
        // 注意清明的需要进行计算的，21世纪的公式为：[Y*D+C]-L
        // 公式解读：Y=年数后2位，D=0.2422，L=闰年数，21世纪C=4.81，20世纪=5.59.
        int param = year - 2000;
        int qingMingDay = (int) (param * 0.2422 + 4.81) - param / 4;
        festivalList.add(new Festival("清明节", 4, qingMingDay, true, 0L));
        festivalList.add(new Festival("元旦", 1, 1, false, 0L));
        festivalList.add(new Festival("春节", 1, 1, true, 0L));
        festivalList.add(new Festival("劳动节", 5, 1, false, 0L));
        festivalList.add(new Festival("端午节", 5, 5, true, 0L));
        festivalList.add(new Festival("中秋节", 8, 15, true, 0L));
        festivalList.add(new Festival("国庆节", 10, 1, false, 0L));
        festivalList.add(new Festival("七夕节", 7, 7, true, 0L));
        festivalList.add(new Festival("情人节", 2, 14, false, 0L));
        festivalList.add(new Festival("重阳节", 9, 9, true, 0L));
        // 获取节日时间差
        festivalList.forEach(this::getGregorianDayDiff);
        // 存放周末 【不剔除法定调休】【周末无需关心月/日】
        festivalList.add(new Festival("周末", 0, 0, false, DateUtil.betweenDay(new Date(), DateUtil.endOfWeek(new Date(), false), false)));
        // 根据时间差排序
        festivalList.sort((Comparator.comparing(Festival::getDiff)));
        // 打印文档
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【摸鱼办】提醒您：").append(DateUtil.format(now, "MM月dd日,")).append(this.timeOfDay(now)).append("好,摸鱼人!\r\n");
        stringBuffer.append("工作再累，一定不要忘记摸鱼哦！有事没事起身去茶水间， 去厕所， 去廊道走走别老在工位上坐着， 钱是老板的, 但命是自己的!\r\n");
        festivalList.forEach(festival -> stringBuffer.append("距离").append(festival.getName()).append("还有不到：").append(festival.getDiff()).append("天\r\n"));
        stringBuffer.append("为了放假加油吧！\n" +
                "上班是帮老板赚钱，摸鱼是赚老板的钱！\n" +
                "最后，祝愿天下所有摸鱼人，都能愉快的渡过每一天！\n");
        // 获取黄历信息
        Lunar date = new Lunar(year, DateUtil.month(now), DateUtil.dayOfMonth(now));
        stringBuffer.append("======================\r\n");
        stringBuffer.append("【今日黄历】 ").append(date.toFullString());
        weChatUtil.sendTextMsg(stringBuffer.toString(), content);
    }

    /**
     * 获取当前日期和节假日的公历日差
     *
     * @param festival 节日
     */
    private void getGregorianDayDiff(Festival festival) {
        // 获取当前日期
        Date now = new Date();
        int year = DateUtil.year(now);
        // 如果当前日期大于等于节日的月份和天数，则年数取下一年
        if (DateUtil.month(now) >= festival.getMonth() && DateUtil.dayOfMonth(now) >= festival.getDay()) {
            year++;
        }
        DateTime festivalDay;
        // 如果节日为农历，则取出对应的公历日
        if (festival.isChineseDate()) {
            festivalDay = new DateTime(new ChineseDate(year, festival.getMonth(), festival.getDay()).getGregorianDate().getTime());
        } else {
            festivalDay = DateUtil.parse(year + "-" + festival.getMonth() + "-" + festival.getDay());
        }
        // 计算当前日期和节日（公历日）的天数差 并保存
        festival.setDiff(DateUtil.betweenDay(now, festivalDay, false));
    }

    /**
     * 获取当前时间的时间段
     *
     * @param date 时间
     * @return
     */
    private String timeOfDay(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String str = df.format(date);
        int a = Integer.parseInt(str);
        if (a >= 0 && a <= 6) {
            return "凌晨";
        }
        if (a > 6 && a <= 11) {
            return "上午";
        }
        if (a > 11 && a <= 13) {
            return "中午";
        }
        if (a > 13 && a <= 18) {
            return "下午";
        }
        if (a > 18 && a <= 24) {
            return "晚上";
        }
        return null;
    }

}
