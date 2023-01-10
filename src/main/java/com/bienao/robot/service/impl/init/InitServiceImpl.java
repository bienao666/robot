package com.bienao.robot.service.impl.init;

import com.bienao.robot.entity.CommandEntity;
import com.bienao.robot.mapper.CommandMapper;
import com.bienao.robot.service.init.InitService;
import com.bienao.robot.service.ql.WireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InitServiceImpl implements InitService {

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private WireService wireService;

    @Override
    @Async("asyncServiceExecutor")
    public void init() {
        initializeCommand();
        initializeWire();
    }

    public void initializeCommand() {
        List<String> commands = commandMapper.queryAllCommand();
        ArrayList<CommandEntity> commandEntities = new ArrayList<>();
        addCommandEntities("我的uid","查询自己的uid","",1,commands,commandEntities);
        addCommandEntities("myuid","查询自己的uid","",1,commands,commandEntities);
        addCommandEntities("群号","查询微信群群号","",1,commands,commandEntities);
        addCommandEntities("群号 xxx","根据微信群名称查询微信群群号","",1,commands,commandEntities);
        addCommandEntities("群号列表","查询所有群号","",1,commands,commandEntities);
        addCommandEntities("监听","监听此群消息","开始监听",1,commands,commandEntities);
        addCommandEntities("取消监听","取消监听此群消息","取消监听成功",1,commands,commandEntities);
        addCommandEntities("q","退出当前操作","已退出",1,commands,commandEntities);
        addCommandEntities("博客","查询博客地址","",1,commands,commandEntities);
        addCommandEntities("登陆","京东短信登录","",1,commands,commandEntities);
        addCommandEntities("饿了么","获取饿了么赚现金推广图片","",1,commands,commandEntities);
        addCommandEntities("支付宝红包","获取支付宝红包商家二维码图片","",1,commands,commandEntities);
        addCommandEntities("微博","当前微博热搜前20条","",1,commands,commandEntities);
        addCommandEntities("监控茅台洋河","监控茅台洋河活动","监控成功",1,commands,commandEntities);
        addCommandEntities("取消监控茅台洋河","取消监控茅台洋河活动","取消茅台洋河监控成功",1,commands,commandEntities);
        addCommandEntities("推送饿了么","定时推送饿了么赚现金推广图片","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送饿了么","取消定时推送饿了么赚现金推广图片","取消饿了么推送成功",1,commands,commandEntities);
        addCommandEntities("推送摸鱼","定时推送摸鱼信息","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送摸鱼","取消定时推送摸鱼信息","取消饿了么推送成功",1,commands,commandEntities);
        addCommandEntities("推送微博","定时推送当前微博热搜前20条","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送微博","取消定时推送当前微博热搜前20条","取消推送微博成功",1,commands,commandEntities);
        addCommandEntities("推送支付宝红包","定时推送支付宝红包","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送支付宝红包","取消定时推送支付宝红包","取消推送支付宝红包成功",1,commands,commandEntities);
        addCommandEntities("举牌 xxx","根据xxx的内容生成一张图片","",1,commands,commandEntities);
        addCommandEntities("xxx油价","查询xxx省的油价","",1,commands,commandEntities);
        addCommandEntities("买家秀","随机获取一张买家秀图片","",1,commands,commandEntities);
        addCommandEntities("老色批","随机获取一张老色批图片","",1,commands,commandEntities);
        addCommandEntities("xx天气","查询xx城市的天气","",1,commands,commandEntities);
        addCommandEntities("比价","根据商品链接查询价格信息","",1,commands,commandEntities);
        addCommandEntities("启用 xxx","启用京东ck xxx","启用成功",1,commands,commandEntities);
        addCommandEntities("禁用 xxx","禁用京东ck xxx","禁用成功",1,commands,commandEntities);
        addCommandEntities("转发 群号1 群号2","转发群号1的消息到群号2","",1,commands,commandEntities);
        addCommandEntities("取消转发 群号1 群号2","取消转发群号1的消到群号2","",1,commands,commandEntities);
        addCommandEntities("扭","随机获取一个小姐姐跳舞短视频","",1,commands,commandEntities);
        if (commandEntities.size()>0){
            commandMapper.addCommands(commandEntities);
        }
    }

    private void addCommandEntities(String command,String function,String reply,Integer isBuiltIn,List<String> commands,ArrayList<CommandEntity> commandEntities){
        if (!commands.contains(command)){
            CommandEntity commandEntity = new CommandEntity();
            commandEntity.setCommand(command);
            commandEntity.setFunction(function);
            commandEntity.setReply(reply);
            commandEntity.setIsBuiltIn(isBuiltIn);
            commandEntities.add(commandEntity);
        }
    }

    public void initializeWire() {
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
        wireService.addWire("邀请赢大礼","jd_prodev.py",keys);

        keys.add("jd_wxShopFollowActivity_activityUrl");
        wireService.addWire("关注店铺有礼","jd_wxShopFollowActivity.js",keys);

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

        keys.add("jd_joinCommonId");
        wireService.addWire("通用开卡-joinCommon","jd_joinCommon_opencard.py",keys);

        keys.add("jd_wxShopGiftId");
        wireService.addWire("特效关注有礼","jd_wxShopGift.py",keys);

        keys.add("jd_cjwxShopFollowActivity_activityId");
        wireService.addWire("CJ关注店铺有礼","jd_cjwxShopFollowActivity.js",keys);

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

        keys.add("categoryUnion_activityId");
        wireService.addWire("品类联合-通用脚本","jd_lzdz_categoryUnion.js",keys);

        keys.add("jd_wxCollectionActivityUrl");
        wireService.addWire("加购有礼-JK","jd_wxCollectionActivity.py",keys);

        keys.add("jinggengInviteJoin");
        wireService.addWire("jinggeng邀请入会有礼","jd_jinggengInvite.py",keys);

        keys.add("jd_wxBirthGiftsId");
        wireService.addWire("生日礼包","jd_wxBirthGifts.py",keys);

        keys.add("jd_wxCompleteInfoId");
        wireService.addWire("完善信息有礼","jd_wxCompleteInfo.py",keys);

        keys.add("jd_cjdaily_activityId");
        wireService.addWire("CJ每日抢好礼通用活动","jd_cjdaily.js",keys);

        keys.add("jd_daily_activityId");
        wireService.addWire("LZ每日抢好礼通用活动","jd_daily.js",keys);

        keys.add("LZKJ_SEVENDAY");
        wireService.addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("LZKJ_SIGN");
        wireService.addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("CJHY_SEVENDAY");
        wireService.addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("CJHY_SIGN");
        wireService.addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("jd_wxShopGift_activityUrl");
        wireService.addWire("店铺礼包特效","jd_wxShopGift.js",keys);

        keys.add("jd_wxKnowledgeActivity_activityUrl");
        wireService.addWire("知识超人","jd_wxKnowledgeActivity.js",keys);

        keys.add("DPQDTK");
        wireService.addWire("常规店铺签到","jd_dpqd.js",keys);

        keys.add("jd_cart_item_activityUrl");
        wireService.addWire("收藏大师-加购有礼","jd_txzj_cart_item.js",keys);

        keys.add("jd_collect_item_activityUrl");
        wireService.addWire("收藏大师-关注有礼","jd_txzj_collect_item.js",keys);

        keys.add("jd_collect_shop_activityUrl");
        wireService.addWire("收藏大师-关注商品","jd_collect_shop.js",keys);

        keys.add("jd_categoryUnion_activityId");
        wireService.addWire("品类联合任务","jd_categoryUnion.js",keys);

        keys.add("jd_lottery_activityUrl");
        wireService.addWire("收藏大师-幸运抽奖","jd_txzj_lottery.js",keys);

        keys.add("jd_wdzfd_activityId");
        wireService.addWire("微定制瓜分福袋","jd_wdzfd.js",keys);

        keys.add("jd_lzkj_loreal_invite_url");
        wireService.addWire("邀请入会有礼（lzkj_loreal）","jd_lzkj_loreal_invite.js",keys);

        keys.add("jd_showInviteJoin_activityUrl");
        wireService.addWire("邀请入会赢好礼（京耕）","jd_jinggeng_showInviteJoin.js",keys);

        keys.add("jd_shopDraw_activityUrl");
        wireService.addWire("店铺左侧刮刮乐","jd_shopDraw.js",keys);

        keys.add("jd_teamAJ");
        wireService.addWire("AJ组队分豆-瓜分","jd_team_exchange.js",keys);

        keys.add("jd_teamFLP");
        wireService.addWire("FLP组队分豆-瓜分","jd_team_exchange.js",keys);

        keys.add("jd_car_play_exchangeid");
        wireService.addWire("组队分豆-头文字J兑换","jd_car_play_exchange.js",keys);

        keys.add("jd_lzkj_loreal_cart_url");
        wireService.addWire("加购有礼（lzkj_loreal）","jd_lzkj_loreal_cart.js",keys);

        keys.add("jd_lzkj_loreal_followShop_url");
        wireService.addWire("关注有礼（lzkj_loreal）","jd_lzkj_loreal_followShop.js",keys);

        keys.add("jd_lzkj_loreal_draw_url");
        wireService.addWire("幸运抽奖（lzkj_loreal）","jd_lzkj_loreal_draw.js",keys);

        /*keys.add("M_WX_SHOP_GIFT_URL");
        addWire("","",keys);

        keys.add("M_WX_FOLLOW_DRAW_URL");
        addWire("","",keys);*/
    }
}
