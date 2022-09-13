package com.bienao.robot;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.entity.TgBot;
import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.WireKeyEntity;
import com.bienao.robot.mapper.WireKeyMapper;
import com.bienao.robot.mapper.WireMapper;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Component
@Slf4j
public class AfterRunner implements ApplicationRunner {

    @Autowired
    private WxService wxService;

    private Cache<String, String> redis = WXConstant.redis;

    @Autowired
    private WireMapper wireMapper;

    @Autowired
    private WireService wireService;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //添加功能列表
        String msg = "菜单列表：\r\n";
        msg += "————青龙区————\r\n";
        msg += "           青龙        \r\n";
        msg += "————功能区————\r\n";
        msg += "           比价  |  油价  \r\n";
        msg += "           监控茅台洋河    \r\n";
        msg += "————娱乐区————\r\n";
        msg += "           摸鱼  |  微博  \r\n";
        msg += "           举牌  |  天气  \r\n";
        msg += "           买家秀     \r\n";
        redis.put("functionList",msg);

        //启动监听
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("开始监听微信信息。。。");
                while (true){
                    EvictingQueue<JSONObject> messageLists = WXConstant.messageList;
                    if (messageLists.size()>0){
                        JSONObject message = messageLists.poll();
                        if (message != null) {
                            wxService.handleMessage(message);
                        }
                    }
                }
            }
        }).start();

        //初始化线报活动
        new Thread(new Runnable() {
            @Override
            public void run() {
                Integer count = wireMapper.queryCount();
                log.info("初始化线报活动。。。");
                ArrayList<String> keys = new ArrayList<>();

                keys.add("jd_cjhy_activityId");
                wireService.addWire("CJ组队瓜分京豆","jd_cjzdgf.js",keys);

                keys.add("jd_redrain_activityId");
                keys.add("jd_redrain_url");
                wireService.addWire("整点京豆雨","jd_redrain.js",keys);

                keys.add("jd_redrain_half_url");
                wireService.addWire("超级直播间半点雨","jd_live_redrain.js",keys);

                keys.add("wish_appIdArrList");
                keys.add("wish_appNameArrList");
                wireService.addWire("许愿池","jd_wish.js",keys);

                keys.add("jd_zdjr_activityId");
                keys.add("jd_zdjr_activityUrl");
                wireService.addWire("zdjr","jd_zdjr.js",keys);

                keys.add("M_WX_LUCK_DRAW_URL");
                wireService.addWire("M幸运抽奖","m_jd_wx_luckDraw.js",keys);

                keys.add("SHOP_TOKENS");
                keys.add("DPQDTK");
                wireService.addWire("店铺签到","jd_shop_sign.js",keys);

                keys.add("jd_nzmhurl");
                wireService.addWire("女装盲盒","jd_nzmh.js",keys);

                keys.add("WXGAME_ACT_ID");
                wireService.addWire("通用游戏任务","jd_game.js",keys);

                keys.add("M_WX_COLLECT_CARD_URL");
                wireService.addWire("M抽奖集卡","m_jd_wx_collectCard.js",keys);

                keys.add("M_WX_ADD_CART_URL");
                wireService.addWire("M加购有礼","m_jd_wx_addCart.js",keys);

                keys.add("jd_mhurlList");
                wireService.addWire("盲盒任务抽京豆","jd_mhtask.js",keys);

                keys.add("jd_wdz_activityId");
                wireService.addWire("微定制","jd_wdz.js",keys);

                keys.add("VENDER_ID");
                wireService.addWire("入会开卡领取礼","jd_OpenCard_Force.js",keys);

                keys.add("jd_wxShareActivity_activityId");
                keys.add("jd_wxShareActivity_helpnum");
                wireService.addWire("分享有礼","jd_wxShareActivity.js",keys);

                keys.add("M_FOLLOW_SHOP_ARGV");
                wireService.addWire("M关注有礼","m_jd_follow_shop.js",keys);

                keys.add("comm_activityIDList");
                wireService.addWire("joy开卡","jd_joyjd_open.js",keys);

                keys.add("computer_activityIdList");
                wireService.addWire("电脑城配件ID任务","jd_computer.js",keys);

                keys.add("PKC_TXGZYL");
                wireService.addWire("pkc特效关注有礼","jd_pkc_txgzyl.js",keys);

                keys.add("PKC_GZYL");
                wireService.addWire("pkc关注有礼","jd_pkc_gzyl.js",keys);

                keys.add("jd_fxyl_activityId");
                wireService.addWire("LZ分享有礼","jd_lzshare.js",keys);

                keys.add("jd_wxSecond_activityId");
                wireService.addWire("读秒拼手速","jd_wxSecond.js",keys);

                keys.add("SEVENDAY_LIST");
                wireService.addWire("超级店铺签到","jd_sevenDay.js",keys);

                keys.add("jd_wxCollectCard_activityId");
                wireService.addWire("集卡抽奖","jd_wxCollectCard.js",keys);

                keys.add("JD_Lottery");
                wireService.addWire("通用抽奖机","jd_lottery.js",keys);

                keys.add("JD_JOYOPEN");
                wireService.addWire("JOY通用开卡活动","jd_joyopen.js",keys);

                keys.add("jd_drawCenter_activityId");
                keys.add("jd_drawCenter_addCart");
                wireService.addWire("LZ刮刮乐抽奖","jd_drawCenter.js",keys);

                keys.add("jd_wxCartKoi_activityId");
                wireService.addWire("LZ购物车锦鲤/购物车锦鲤通用活动","jd_wxCartKoi.js",keys);

                keys.add("jd_wxFansInterActionActivity_activityId");
                wireService.addWire("LZ粉丝互动","jd_wxFansInterActionActivity.js",keys);

                keys.add("yhyactivityId");
                keys.add("yhyauthorCode");
                wireService.addWire("邀请赢大礼/邀好友赢大礼","jd_yqhy.py",keys);

                keys.add("jd_wxShopFollowActivity_activityId");
                wireService.addWire("关注抽奖","jd_wxShopFollowActivity.js",keys);

                keys.add("jd_wxUnPackingActivity_activityId");
                wireService.addWire("LZ让福袋飞通用活动","jd_wxUnPackingActivity.js",keys);

                keys.add("LUCK_DRAW_URL");
                keys.add("LUCK_DRAW_OPENCARD");
                keys.add("LUCK_DRAW_NUM");
                wireService.addWire("店铺抽奖通用活动","jd_luck_draw.js",keys);

                keys.add("jd_lzaddCart_activityId");
                wireService.addWire("LZ加购有礼","jd_lzaddCart.js",keys);

                keys.add("jd_wxgame_activityId");
                keys.add("jd_wxgame_addCart");
                wireService.addWire("LZ店铺游戏","jd_wxgame.js",keys);

                keys.add("jd_shopCollectGiftId");
                wireService.addWire("一键领取京豆-店铺会员礼包","jd_shopCollectGift.py",keys);

                keys.add("jd_shopLeagueId");
                wireService.addWire("通用开卡-shopLeague系列","jd_shopLeague_opencard.py",keys);

                keys.add("jd_wdz_activityId");
                wireService.addWire("微定制组队通用脚本","jd_wdz.py",keys);

                keys.add("jd_joinCommonId");
                wireService.addWire("通用开卡-joinCommon","jd_joinCommon_opencard.py",keys);

                keys.add("jd_wxShopGiftId");
                wireService.addWire("特效关注有礼","jd_wxShopGift.py",keys);

                keys.add("jd_cjwxShopFollowActivity_activityId");
                wireService.addWire("CJ关注店铺有礼","jd_cjwxShopFollowActivity.js",keys);

                keys.add("jd_wxKnowledgeActivity_activityId");
                wireService.addWire("LZ知识超人通用活动","jd_wxKnowledgeActivity.js",keys);

                keys.add("jd_cjwxKnowledgeActivity_activityId");
                wireService.addWire("CJ知识超人通用活动","jd_cjwxKnowledgeActivity.js",keys);

                keys.add("jd_wxBuildActivity_activityId");
                wireService.addWire("LZ盖楼有礼","jd_wxBuildActivity.js",keys);

                keys.add("jd_wxMcLevelAndBirthGifts_activityId");
                keys.add("jd_wxMcLevelAndBirthGifts_activityUrl");
                wireService.addWire("生日礼包和会员等级礼包","jd_wxMcLevelAndBirthGifts.js",keys);

                keys.add("jd_completeInfoActivity_activityId");
                keys.add("jd_completeInfoActivity_venderId");
                keys.add("jd_completeInfoActivity_activityUrl");
                wireService.addWire("完善信息有礼","jd_completeInfoActivity.js",keys);

                keys.add("DPLHTY");
                wireService.addWire("大牌联合通用开卡","jd_opencardDPLHTY.js",keys);

                keys.add("jd_wxCollectionActivity_activityUrl");
                wireService.addWire("加购物车抽奖","jd_wxCollectionActivity.js",keys);

                keys.add("jd_inv_authorCode");
                wireService.addWire("邀好友赢大礼","jd_inviteFriendsGift.py",keys);
            }
        }).start();

        //初始化tgbot
        DefaultBotOptions botOptions = new DefaultBotOptions();
        List<SystemParam> SystemParams = systemParamUtil.querySystemParams("TGPROXY");
        String tgProxy = SystemParams.get(0).getValue();
        if (StringUtils.isNotEmpty(tgProxy) && tgProxy.contains(":")){
            String[] split = tgProxy.split(":");
            //梯子在自己电脑上就写127.0.0.1  软路由就写路由器的地址
            String proxyHost = split[0];
            //端口根据实际情况填写，说明在上面，自己看
            int proxyPort = Integer.parseInt(split[1]);

            botOptions.setProxyHost(proxyHost);
            botOptions.setProxyPort(proxyPort);
        }
        //注意一下这里，ProxyType是个枚举，看源码你就知道有NO_PROXY,HTTP,SOCKS4,SOCKS5;
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);

        DefaultBotSession defaultBotSession = new DefaultBotSession();
        defaultBotSession.setOptions(botOptions);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
            SystemParams = systemParamUtil.querySystemParams("TGBOTTOKEN");
            String tgBotToken = SystemParams.get(0).getValue();
            SystemParams = systemParamUtil.querySystemParams("TGBOTUSERNAME");
            String tgBotUsername = SystemParams.get(0).getValue();
            if (StringUtils.isNotEmpty(tgProxy) && tgProxy.contains(":")){
                //需要代理
                TgBot bot = new TgBot(botOptions);
                bot.setToken(tgBotToken);
                bot.setUsername(tgBotUsername);
                telegramBotsApi.registerBot(bot);
            }else {
                //不需代理
                TgBot bot = new TgBot();
                bot.setToken(tgBotToken);
                bot.setUsername(tgBotUsername);
                telegramBotsApi.registerBot(bot);
            }
        } catch (TelegramApiException e) {
            log.info("tgbot连接失败，请检查配置");
            e.printStackTrace();
        }
    }
}
