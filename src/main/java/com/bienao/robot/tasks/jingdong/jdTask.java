package com.bienao.robot.tasks.jingdong;

import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.service.jingdong.JdService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

    private SystemParamUtil systemParamUtil;

    @Value("${task-function.shareHelp}")
    private boolean shareHelp;

    @Value("${task-function.resetCkStatus}")
    private boolean resetCkStatus;

    @Value("${task-function.checkCk}")
    private boolean checkCk;

    @Value("${task-function.updateShareCode}")
    private boolean updateShareCode;

    @Value("${task-function.countJd}")
    private boolean countJd;

    /**
     * 助力池互助
     */
    @Scheduled(cron = "20 0 0 * * ?")
    public void shareHelp(){
        if (shareHelp){
            //重置助力
            ckService.resetCkStatus();
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
     * 助力池互助
     */
    @Scheduled(cron = "20 0 2,6,12,18 * * ?")
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
     * 检查ck是否过期
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void checkCk(){
        if (checkCk){
            ckService.checkCk();
        }else {
            log.info("检查ck是否过期定时任务执行失败，请先去配置checkCk为true");
        }
    }

    /**
     * 维护助力码
     */
    @Scheduled(cron = "0 0 12,18,23 * * ?")
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
    }

    /**
     * 统计京豆收益
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void countJd(){
        if (countJd){
            jdService.countJd();
        }else {
            log.info("统计京豆收益定时任务执行失败，请先去配置countJd为true");
        }
    }
}
