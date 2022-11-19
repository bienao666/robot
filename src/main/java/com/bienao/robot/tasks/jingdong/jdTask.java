package com.bienao.robot.tasks.jingdong;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.CacheObj;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.PatternConstant;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.entity.jingdong.JdZqdyjEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.jingdong.JdZqdyjMapper;
import com.bienao.robot.redis.Redis;
import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.service.jingdong.JdDhService;
import com.bienao.robot.service.jingdong.JdService;
import com.bienao.robot.service.jingdong.ZqdyjService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 京东定时任务
 */
@Component
@Slf4j
public class jdTask {

    @Autowired
    private JdService jdService;

    @Autowired
    private CkService ckService;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Value("${task-function.shareHelp}")
    private boolean shareHelp;

    @Value("${task-function.resetCkStatus}")
    private boolean resetCkStatus;

    @Value("${task-function.checkZlc}")
    private boolean checkZlc;

    @Value("${task-function.updateShareCode}")
    private boolean updateShareCode;

    @Value("${task-function.countJd}")
    private boolean countJd;

    @Autowired
    private ZqdyjService zqdyjService;

    private Cache<String, String> redis = WXConstant.redis;

    /**
     * 助力池互助
     */
    @Scheduled(cron = "30 0 0 * * ?")
    public void shareHelp(){
        if (shareHelp){
            //重置助力
            ckService.resetCkStatus();
            //接口调用等待时间
            int zlcwaittime = 0;
            String zlcwaittimeStr = systemParamUtil.querySystemParam("ZLCWAITTIME");
            if (StringUtils.isEmpty(zlcwaittimeStr)){
                zlcwaittime = 30;
            }else {
                zlcwaittime = Integer.parseInt(zlcwaittimeStr);
            }
            //查询所有ck
            List<JdCkEntity> cks = jdService.queryCksAndActivity();

            //东东农场
            jdService.fruitShareHelp(cks,zlcwaittime);

            //东东萌宠
            jdService.petShareHelp(cks, zlcwaittime);

            //种豆得豆
            jdService.plantShareHelp(cks,zlcwaittime);
        }else {
            log.info("助力池互助定时任务执行失败，请先去配置shareHelp为true");
        }
    }

    /**
     * 助力池互助
     */
    @Scheduled(cron = "0 */30 1-23 * * ?")
    public void shareHelpNoReset(){
        if (shareHelp){
            //接口调用等待时间
            int zlcwaittime = 0;
            String zlcwaittimeStr = systemParamUtil.querySystemParam("ZLCWAITTIME");
            if (StringUtils.isEmpty(zlcwaittimeStr)){
                zlcwaittime = 10;
            }else {
                zlcwaittime = Integer.parseInt(zlcwaittimeStr);
            }
            //查询所有ck
            List<JdCkEntity> cks = jdService.queryCksAndActivity();

            //东东农场
            jdService.fruitShareHelp(cks,zlcwaittime);

            //东东萌宠
            jdService.petShareHelp(cks, zlcwaittime);

            //种豆得豆
            jdService.plantShareHelp(cks,zlcwaittime);
        }else {
            log.info("助力池互助定时任务执行失败，请先去配置shareHelp为true");
        }
    }

    /**
     * 检查助力池是否过期
     */
    @Scheduled(cron = "0 0 13 * * ?")
    public void checkZlc(){
        if (checkZlc){
            ckService.checkZlc();
        }else {
            log.info("检查助力池是否过期定时任务执行失败，请先去配置checkZlc为true");
        }
    }

    /**
     * 维护助力码
     */
    /*@Scheduled(cron = "0 0 12,22 * * ?")
    public void updateShareCode(){
        if (updateShareCode){
            List<JdCkEntity> cks = jdService.queryCksAndActivity();
            log.info("开始维护东东农场互助码...");
            jdService.updateFruitShareCode(cks);
            log.info("开始维护东东萌宠互助码...");
            jdService.updatePetShareCode(cks);
            log.info("开始维护种豆得豆互助码...");
            jdService.updatePlantShareCode(cks);
        }else {
            log.info("维护助力码定时任务执行失败，请先去配置updateShareCode为true");
        }
    }*/

    /**
     * 统计京豆收益
     */
    @Scheduled(cron = "0 30 */3 * * ?")
    public void countJd(){
        if (countJd){
            jdService.countJd();
        }else {
            log.info("统计京豆收益定时任务执行失败，请先去配置countJd为true");
        }
    }

    /**
     * 青龙同步助力池
     * 每隔十分钟同步一次
     */
    @Scheduled(cron = "30 */5 * * * ?")
    public void qlToZlc(){
        String qltozlc = systemParamUtil.querySystemParam("QLTOZLC");
        if ("是".equals(qltozlc)){
            jdService.qlToZlc();
        }
    }

    /**
     * 清空京东查询次数限制
     */
    @Scheduled(cron = "0 59 23 * * ?")
    public void clearJdQueryTimes(){
        ArrayList<String> clear = new ArrayList<>();
        Iterator<CacheObj<String, String>> iterator = redis.cacheObjIterator();
        if (iterator.hasNext()){
            CacheObj<String, String> cacheObj = iterator.next();
            String key = cacheObj.getKey();
            if (key.endsWith("QueryTimes")){
                clear.add(key);
            }
        }
        for (String k : clear) {
            redis.remove(k);
        }
    }

    /**
     * 京东定时任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void jdTimeTask(){
        ckService.jkExchange();
    }

    /**
     * 大赢家定时助力
     */
    @Scheduled(cron = "0 0-30/5 0 * * ?")
    public void helpZqdyj(){
        String zqdyjhelp = Redis.redis.get("ZQDYJHELP");
        if (StringUtils.isNotEmpty(zqdyjhelp)){
            log.info("正在助力中其他账号中，跳过");
            return;
        }

        //获取所有 有效的 还有助力的 非火爆的ck
        List<JSONObject> zqdyjCk = zqdyjService.getZqdyjCk();
        zqdyjCk = zqdyjCk.stream().filter(jsonObject -> "0".equals(jsonObject.getString("isHei"))
                && "1".equals(jsonObject.getString("toHelpStatus"))).collect(Collectors.toList());

        if (zqdyjCk.size()==0){
            log.info("赚钱大赢家没有可助力的账号了！！！");
            return ;
        }
        Collections.shuffle(zqdyjCk);

        //获取待助力的账号
        List<JdZqdyjEntity> helpList = zqdyjService.getHelpList();
        for (JdZqdyjEntity jdZqdyj : helpList) {
            if (jdZqdyj.getHelpStatus() == 1) {
                continue;
            }
            try {
                Integer id = jdZqdyj.getId();
                String helpCode = jdZqdyj.getHelpCode();
                String remark = jdZqdyj.getRemark();
                String ck = jdZqdyj.getCk();
                Matcher matcher = PatternConstant.ckPattern.matcher(ck);
                String needHelpPtPin = "";
                if (matcher.find()){
                    needHelpPtPin = matcher.group(2);
                }
                zqdyjService.help(id,needHelpPtPin,helpCode,ck,remark,zqdyjCk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清理无效的数据
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void clear(){
        jdService.clear();
    }

    /**
     * 重置赚钱大赢家
     */
    @Scheduled(cron = "0 59 23 * * ?")
    public void zqdyjReset(){
        zqdyjService.reset();
    }
}
