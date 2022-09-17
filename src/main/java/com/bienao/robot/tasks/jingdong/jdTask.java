package com.bienao.robot.tasks.jingdong;

import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.service.jingdong.JdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
            jdService.shareHelp(true);
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
            jdService.shareHelp(false);
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
    @Scheduled(cron = "0 0 23 * * ?")
    public void updateShareCode(){
        if (updateShareCode){
            jdService.updateShareCode();
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
