package com.bienao.robot.service.impl.ql;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.*;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.mapper.WireKeyMapper;
import com.bienao.robot.mapper.WireMapper;
import com.bienao.robot.mapper.WirelistMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.redis.Redis;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.utils.ql.QlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class WireServiceImpl implements WireService {

    @Autowired
    private WireMapper wireMapper;

    @Autowired
    private WireKeyMapper wireKeyMapper;

    @Autowired
    private QlMapper qlMapper;

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private WirelistMapper wirelistMapper;

    @Autowired
    private QlService qlService;

    /**
     * 添加线报活动
     * @param wireEntity
     * @return
     */
    @Override
    @Transactional
    public Result addWire(WireEntity wireEntity) {
        int i = wireMapper.addWire(wireEntity);
        if (i==0) {
            throw new RuntimeException("数据库操作异常");
        }
        Integer maxId = wireMapper.queryMaxId();
        List<WireKeyEntity> keys = wireEntity.getKeys();
        for (WireKeyEntity key : keys) {
            i = wireKeyMapper.addWireKey(maxId, key.getKey());
            if (i==0){
                throw new RuntimeException("数据库操作异常");
            }
        }
        return Result.success("操作成功");
    }

    /**
     * 添加线报活动
     * @param
     * @return
     */
    @Override
    public void addWire(String activityName,String script,List<String> keys){
        try {
            WireEntity wireEntity = new WireEntity(activityName,script);
            Integer res = wireMapper.addWire(wireEntity);
            if (res!=0){
                Integer maxId = wireMapper.queryMaxId();
                for (String key : keys) {
                    wireKeyMapper.addWireKey(maxId,key);
                }
            }
        } catch (Exception e) {
            //todo
        }
        keys.clear();
    }

    /**
     * 修改线报活动
     * @param wireEntity
     */
    @Override
    public Result updateWire(WireEntity wireEntity) {
        wireEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
        wireMapper.updateWire(wireEntity);
        List<WireKeyEntity> keys = wireEntity.getKeys();
        if (keys!=null && keys.size()>0){
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(wireEntity.getId());
            wireKeyMapper.deleteWireKey(ids);
            for (WireKeyEntity key : wireEntity.getKeys()) {
                wireKeyMapper.addWireKey(wireEntity.getId(),key.getKey());
            }
        }
        return Result.success("修改成功");
    }

    /**
     * 删除线报活动
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Result deleteWire(List<Integer> ids) {
        Integer res = wireMapper.deleteWire(ids);
        if (res==0){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"删除失败");
        }else {
            res = wireKeyMapper.deleteWireKey(ids);
            if (res==0){
                throw new RuntimeException("数据库操作异常");
            }else {
                return Result.success();
            }
        }
    }

    /**
     * 执行线报活动
     * @param script
     * @param wire
     * @return
     */
    @Override
    public Result handleActivity(Integer wireListId,String script,String wire) {
        log.info("执行线报活动：{}->{}",script,wire);

        //是否需要设置大车头
        WireEntity wireEntity = wireMapper.queryWire(script);
        if (wireEntity.getSetHead()==1){
            //配置大车头
            qlService.oneKeyHead();
        }

        ArrayList<String> result = new ArrayList<>();
        List<String> list = Arrays.asList(wire.split("\\r?\\n"));
        ArrayList<String> keys = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            boolean configFlag = false;
            //设置参数
            try {
                String configs = qlUtil.getFile(ql.getUrl(), ql.getTokenType(), ql.getToken(), "config.sh");
                for (String config : list) {
                    if (!config.contains("export")){
                        continue;
                    }
                    if (config.contains("并发变量") || config.contains("你的助力码")){
                        continue;
                    }
                    if (config.contains("=")){
                        StringBuffer stringBuffer = new StringBuffer(configs);
                        //export 参数名
                        String[] split = config.split("=");
                        String s1 = split[0];
                        String key = s1.replace("export", "").replace(" ", "");
                        keys.add(key);
                        //参数值
                        String s2 = split[1];
                        if (split.length>2){
                            for (int i = 2; i < split.length; i++) {
                                s2 = s2 + "=" + split[i];
                            }
                        }
                        if (configs.contains(s1)){
                            //配置过
                            int s1index = configs.indexOf(s1);
                            int first = configs.indexOf("\"", s1index + s1.length());
                            int last = configs.indexOf("\"", first+1)+1;
                            configs = stringBuffer.replace(first,last,s2).toString();
                        }else {
                            //没配置过
                            configs += "\n" + config;
                        }
                    }
                }
                boolean flag = qlUtil.saveFile(ql.getUrl(), ql.getTokenType(), ql.getToken(), "config.sh", configs);
                if (flag){
                    //配置成功
                    result.add(ql.getUrl() + "(" + ql.getRemark() + ")" + " 配置 成功");
                    configFlag = true;
                }else {
                    //配置失败
                    result.add(ql.getUrl() + "(" + ql.getRemark() + ")" + " 配置 失败");
                    configFlag = false;
                    throw new RuntimeException("配置失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.add(ql.getUrl() + "(" + ql.getRemark() + ")" + " 线报 未执行，请手动配置执行");
            }

            if (configFlag){
                //执行脚本
                String remark = ql.getRemark();
                String url = ql.getUrl();
                try {
                    List<QlCron> crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                    if (crons == null) {
                        //重试一次
                        crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                    }
                    if (crons != null) {
                        Integer old = result.size();
                        for (QlCron cron : crons) {
                            if (cron.getCommand().contains(script)) {
                                Integer id = cron.getId();
                                List<Integer> cronIds = new ArrayList<>();
                                cronIds.add(id);
                                qlUtil.stopCron(url, ql.getTokenType(), ql.getToken(), cronIds);
                                boolean flag = qlUtil.runCron(url, ql.getTokenType(), ql.getToken(), cronIds);
                                if (flag) {
                                    result.add(url + "(" + remark + ")" + " 线报 执行成功");
                                    Thread.sleep(4000);
//                                    handleFlag = true;
                                } else {
                                    result.add(url + "(" + remark + ")" + " 线报 执行失败，请手动执行");
                                }
                                break;
                            }
                        }
                        Integer now = result.size();
                        if (old.equals(now)) {
                            result.add(url + "(" + remark + ")" + script + " 脚本不存在，请拉库后执行");
                        }
                    } else {
                        result.add(url + "(" + remark + ")" + " 线报 执行失败，请手动执行");
                    }

                } catch (Exception e) {
                    result.add(url + "(" + remark + ")" + "线报 执行失败，请手动执行");
                    e.printStackTrace();
                }
            }
        }

        if (wireEntity.getSetHead()==1){
            //延迟取消大车头
            qlService.waitCancelHead();
        }

        //更新线报表
        if (result.size()!=0){
            wirelistMapper.updateWirelist(wireListId,JSONObject.toJSONString(result),DateUtil.formatDateTime(new Date()));
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.SERVICE_ERROR,"线报执行异常");
        }
    }

    /**
     * 添加线报活动
     * @param wire
     * @return
     */
    @Override
    public Result addActivity(String wire) {
        List<WireEntity> wires = new ArrayList<>();
        List<String> list = Arrays.asList(wire.split("\\r?\\n"));
        StringBuilder s = new StringBuilder();
        for (String config : list) {
            if (config.contains("#") && (config.contains(".js") || config.contains(".py"))){
                continue;
            }
            if (config.contains("=")){
                //export 参数名
                String s1 = config.split("=")[0];
                int index = s1.indexOf("export");
                if (index!=0){
                    s1 = s1.substring(index);
                }
                String key = s1.replace("export", "").replace(" ", "");
                String value = config.split("=")[1];
                s.append(key).append(value);
                List<WireEntity> wireEntities = wireKeyMapper.queryScript(key);
                if (wireEntities != null) {
                    for (WireEntity wireEntity : wireEntities) {
                        if (wireEntity.getStatus() != 1) {
                            wires.add(wireEntity);
                        }
                    }
                    break;
                }
            }
        }
        String redis = Redis.wireRedis.get(s.toString(),false);
        if (StringUtils.isNotEmpty(redis)){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"该线报活动已存在，添加失败");
        }
        Redis.wireRedis.put(s.toString(),"1",24 * 60 * 60 * 1000);

        if (wires.size()==0){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"添加失败，线报不存在，请先添加");
        }

        for (WireEntity wireEntity : wires) {
            wirelistMapper.addActivity(wireEntity.getScript(),wire);
            Integer maxId = wirelistMapper.queryMaxId();
            handleWire(maxId,wireEntity.getScript(),wire);
        }
        return Result.success("添加成功");
    }

    /**
     * 查询待执行线报活动
     * @param
     * @return
     */
    @Override
    public Result queryActivity(Integer pageNo,Integer pageSize, String content) {
        List<WireActivityEntity> wireActivityEntities = wirelistMapper.queryActivity(content);
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", wireActivityEntities.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        wireActivityEntities = wireActivityEntities.subList(start, end < wireActivityEntities.size() ? end : wireActivityEntities.size());
        result.put("wireActivityList",wireActivityEntities);
        return Result.success(result);
    }

    /**
     * 查询线报活动
     * @param key
     * @return
     */
    @Override
    public Result queryWire(String key,Integer pageNo,Integer pageSize) {
        List<WireEntity> wireEntities = wireMapper.queryWires(key);
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", wireEntities.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        wireEntities = wireEntities.subList(start, end < wireEntities.size() ? end : wireEntities.size());
        result.put("wireList",wireEntities);
        return Result.success(result);
    }

    /**
     * 执行线报
     * @param id
     * @param script
     * @param content
     */
    @Override
    public void handleWire(Integer id, String script, String content) {
        //青龙id->脚本列表
        TreeMap<Integer, List<QlCron>> qlIdToScripts = new TreeMap<>();
        //青龙id->活动id
        TreeMap<Integer, Integer> qlIdToCronId = new TreeMap<>();
        //所有青龙cron状态
        ArrayList<Integer> status = new ArrayList<>();
        //查询所有青龙
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (int i = 0; i < qlEntities.size(); i++) {
            QlEntity qlEntity = qlEntities.get(i);
            List<QlCron> crons = qlUtil.getCrons(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
            qlIdToScripts.put(i,crons);
        }

        //查询所有青龙该任务状态
        for (int i = 0; i < qlIdToScripts.entrySet().size(); i++) {
            List<QlCron> qlCrons = qlIdToScripts.get(i);
            for (QlCron qlCron : qlCrons) {
                if (qlCron.getCommand().contains(script)){
                    qlIdToCronId.put(i,qlCron.getId());
                    status.add(qlCron.getStatus());
                    break;
                }
            }
        }
        //该任务都是未运行中
        if (!status.contains(0)){
            //执行该任务
            try {
                handleActivity(id,script,content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Async("asyncServiceExecutor")
    @Override
    public void initializeWire() {
        log.info("初始化线报活动。。。");
        ArrayList<String> keys = new ArrayList<>();

        keys.add("jd_cjhy_activityId");
        addWire("CJ组队瓜分京豆","jd_cjzdgf.js",keys);

        keys.add("jd_redrain_activityId");
        keys.add("jd_redrain_url");
        addWire("整点京豆雨","jd_redrain.js",keys);

        keys.add("jd_redrain_half_url");
        addWire("超级直播间半点雨","jd_live_redrain.js",keys);

        keys.add("wish_appIdArrList");
        keys.add("wish_appNameArrList");
        addWire("许愿池","jd_wish.js",keys);

        keys.add("jd_zdjr_activityId");
        keys.add("jd_zdjr_activityUrl");
        addWire("zdjr","jd_zdjr.js",keys);

        keys.add("M_WX_LUCK_DRAW_URL");
        addWire("M幸运抽奖","m_jd_wx_luckDraw.js",keys);

        keys.add("jd_nzmhurl");
        addWire("女装盲盒","jd_nzmh.js",keys);

        keys.add("WXGAME_ACT_ID");
        addWire("通用游戏任务","jd_game.js",keys);

        keys.add("M_WX_COLLECT_CARD_URL");
        addWire("M抽奖集卡","m_jd_wx_collectCard.js",keys);

        keys.add("M_WX_ADD_CART_URL");
        addWire("M加购有礼","m_jd_wx_addCart.js",keys);

        keys.add("jd_mhurlList");
        addWire("盲盒任务抽京豆","jd_mhtask.js",keys);

        keys.add("jd_wdz_activityId");
        addWire("微定制","jd_wdz.js",keys);

        keys.add("VENDER_ID");
        addWire("入会开卡领取礼","jd_OpenCard_Force.js",keys);

        keys.add("jd_wxShareActivity_activityId");
        keys.add("jd_wxShareActivity_helpnum");
        addWire("分享有礼","jd_wxShareActivity.js",keys);

        keys.add("M_FOLLOW_SHOP_ARGV");
        addWire("M关注有礼","m_jd_follow_shop.js",keys);

        keys.add("comm_activityIDList");
        addWire("joy开卡","jd_joyjd_open.js",keys);

        keys.add("computer_activityIdList");
        addWire("电脑城配件ID任务","jd_computer.js",keys);

        keys.add("PKC_TXGZYL");
        addWire("pkc特效关注有礼","jd_pkc_txgzyl.js",keys);

        keys.add("PKC_GZYL");
        addWire("pkc关注有礼","jd_pkc_gzyl.js",keys);

        keys.add("jd_fxyl_activityId");
        addWire("LZ分享有礼","jd_lzshare.js",keys);

        keys.add("jd_wxSecond_activityId");
        addWire("读秒拼手速","jd_wxSecond.js",keys);

        keys.add("SEVENDAY_LIST");
        addWire("超级店铺签到","jd_sevenDay.js",keys);

        keys.add("jd_wxCollectCard_activityId");
        addWire("集卡抽奖","jd_wxCollectCard.js",keys);

        keys.add("JD_Lottery");
        addWire("通用抽奖机","jd_lottery.js",keys);

        keys.add("JD_JOYOPEN");
        addWire("JOY通用开卡活动","jd_joyopen.js",keys);

        keys.add("jd_drawCenter_activityId");
        keys.add("jd_drawCenter_addCart");
        addWire("LZ刮刮乐抽奖","jd_drawCenter.js",keys);

        keys.add("jd_wxCartKoi_activityId");
        addWire("LZ购物车锦鲤/购物车锦鲤通用活动","jd_wxCartKoi.js",keys);

        keys.add("jd_wxFansInterActionActivity_activityId");
        addWire("LZ粉丝互动","jd_wxFansInterActionActivity.js",keys);

        keys.add("yhyactivityId");
        keys.add("yhyauthorCode");
        addWire("邀请赢大礼","jd_prodev.py",keys);

        keys.add("jd_wxShopFollowActivity_activityUrl");
        addWire("关注店铺有礼","jd_wxShopFollowActivity.js",keys);

        keys.add("jd_wxUnPackingActivity_activityId");
        addWire("LZ让福袋飞通用活动","jd_wxUnPackingActivity.js",keys);

        keys.add("LUCK_DRAW_URL");
        keys.add("LUCK_DRAW_OPENCARD");
        keys.add("LUCK_DRAW_NUM");
        addWire("店铺抽奖通用活动","jd_luck_draw.js",keys);

        keys.add("jd_lzaddCart_activityId");
        addWire("LZ加购有礼","jd_lzaddCart.js",keys);

        keys.add("jd_wxgame_activityId");
        keys.add("jd_wxgame_addCart");
        addWire("LZ店铺游戏","jd_wxgame.js",keys);

        keys.add("jd_shopCollectGiftId");
        addWire("一键领取京豆-店铺会员礼包","jd_shopCollectGift.py",keys);

        keys.add("jd_shopLeagueId");
        addWire("通用开卡-shopLeague系列","jd_shopLeague_opencard.py",keys);

        keys.add("jd_joinCommonId");
        addWire("通用开卡-joinCommon","jd_joinCommon_opencard.py",keys);

        keys.add("jd_wxShopGiftId");
        addWire("特效关注有礼","jd_wxShopGift.py",keys);

        keys.add("jd_cjwxShopFollowActivity_activityId");
        addWire("CJ关注店铺有礼","jd_cjwxShopFollowActivity.js",keys);

        keys.add("jd_wxBuildActivity_activityId");
        addWire("LZ盖楼有礼","jd_wxBuildActivity.js",keys);

        keys.add("jd_wxMcLevelAndBirthGifts_activityId");
        keys.add("jd_wxMcLevelAndBirthGifts_activityUrl");
        addWire("生日礼包和会员等级礼包","jd_wxMcLevelAndBirthGifts.js",keys);

        keys.add("jd_completeInfoActivity_activityId");
        keys.add("jd_completeInfoActivity_venderId");
        keys.add("jd_completeInfoActivity_activityUrl");
        addWire("完善信息有礼","jd_completeInfoActivity.js",keys);

        keys.add("DPLHTY");
        addWire("大牌联合通用开卡","jd_opencardDPLHTY.js",keys);

        keys.add("jd_wxCollectionActivity_activityUrl");
        addWire("加购物车抽奖","jd_wxCollectionActivity.js",keys);

        keys.add("jd_inv_authorCode");
        addWire("邀好友赢大礼","jd_inviteFriendsGift.py",keys);

        keys.add("categoryUnion_activityId");
        addWire("品类联合-通用脚本","jd_lzdz_categoryUnion.js",keys);

        keys.add("jd_wxCollectionActivityUrl");
        addWire("加购有礼-JK","jd_wxCollectionActivity.py",keys);

        keys.add("jinggengInviteJoin");
        addWire("jinggeng邀请入会有礼","jd_jinggengInvite.py",keys);

        keys.add("jd_wxBirthGiftsId");
        addWire("生日礼包","jd_wxBirthGifts.py",keys);

        keys.add("jd_wxCompleteInfoId");
        addWire("完善信息有礼","jd_wxCompleteInfo.py",keys);

        keys.add("jd_cjdaily_activityId");
        addWire("CJ每日抢好礼通用活动","jd_cjdaily.js",keys);

        keys.add("jd_daily_activityId");
        addWire("LZ每日抢好礼通用活动","jd_daily.js",keys);

        keys.add("LZKJ_SEVENDAY");
        addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("LZKJ_SIGN");
        addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("CJHY_SEVENDAY");
        addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("CJHY_SIGN");
        addWire("超级无线店铺签到","jd_sevenDay.js",keys);

        keys.add("jd_wxShopGift_activityUrl");
        addWire("店铺礼包特效","jd_wxShopGift.js",keys);

        keys.add("jd_wxKnowledgeActivity_activityUrl");
        addWire("知识超人","jd_wxKnowledgeActivity.js",keys);

        keys.add("DPQDTK");
        addWire("常规店铺签到","jd_dpqd.js",keys);

        keys.add("jd_cart_item_activityUrl");
        addWire("收藏大师-加购有礼","jd_txzj_cart_item.js",keys);

        keys.add("jd_collect_item_activityUrl");
        addWire("收藏大师-关注有礼","jd_txzj_collect_item.js",keys);

        keys.add("jd_collect_shop_activityUrl");
        addWire("收藏大师-关注商品","jd_collect_shop.js",keys);

        keys.add("jd_categoryUnion_activityId");
        addWire("品类联合任务","jd_categoryUnion.js",keys);

        keys.add("jd_lottery_activityUrl");
        addWire("收藏大师-幸运抽奖","jd_txzj_lottery.js",keys);

        keys.add("jd_wdzfd_activityId");
        addWire("微定制瓜分福袋","jd_wdzfd.js",keys);

        keys.add("jd_lzkj_loreal_invite_url");
        addWire("邀请入会有礼（lzkj_loreal）","jd_lzkj_loreal_invite.js",keys);

        keys.add("jd_showInviteJoin_activityUrl");
        addWire("邀请入会赢好礼（京耕）","jd_jinggeng_showInviteJoin.js",keys);

        keys.add("jd_shopDraw_activityUrl");
        addWire("店铺左侧刮刮乐","jd_shopDraw.js",keys);

        keys.add("jd_teamAJ");
        addWire("组队分豆-瓜分","jd_team_exchange.js",keys);

        keys.add("jd_teamFLP");
        addWire("组队分豆-瓜分","jd_team_exchange.js",keys);

        /*keys.add("M_WX_SHOP_GIFT_URL");
        addWire("","",keys);

        keys.add("M_WX_FOLLOW_DRAW_URL");
        addWire("","",keys);*/
    }


}
