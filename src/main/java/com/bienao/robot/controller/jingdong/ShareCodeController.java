package com.bienao.robot.controller.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.jingdong.JdService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 互助池
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/jdHelp")
public class ShareCodeController {

    @Autowired
    private JdService jdService;

    @Autowired
    private JdCkMapper jdCkMapper;

    @Autowired
    private SystemParamUtil systemParamUtil;

    /**
     * 东东农场互助
     */
    @LoginToken
    @GetMapping("/fruitShareHelp")
    public Result fruitShareHelp(){
        try {
            //接口调用等待时间
            int zlcwaittime = 0;
            String zlcwaittimeStr = systemParamUtil.querySystemParam("ZLCWAITTIME");
            if (StringUtils.isEmpty(zlcwaittimeStr)){
                zlcwaittime = 30;
            }else {
                zlcwaittime = Integer.parseInt(zlcwaittimeStr);
            }

            //查询所有ck
            List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
            jdService.fruitShareHelp(cks,zlcwaittime);
            return Result.success("东东农场互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东农场互助异常");
        }
    }

    /**
     * 东东萌宠互助
     */
    @LoginToken
    @GetMapping("/petShareHelp")
    public Result petShareHelp(){
        try {
            //接口调用等待时间
            int zlcwaittime = 0;
            String zlcwaittimeStr = systemParamUtil.querySystemParam("ZLCWAITTIME");
            if (StringUtils.isEmpty(zlcwaittimeStr)){
                zlcwaittime = 30;
            }else {
                zlcwaittime = Integer.parseInt(zlcwaittimeStr);
            }

            //查询所有ck
            List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
            jdService.petShareHelp(cks,zlcwaittime);
            return Result.success("东东萌宠互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东萌宠互助异常");
        }
    }

    /**
     * 种豆得豆互助
     */
    @LoginToken
    @GetMapping("/plantShareHelp")
    public Result plantShareHelp(){
        try {
            //接口调用等待时间
            int zlcwaittime = 0;
            String zlcwaittimeStr = systemParamUtil.querySystemParam("ZLCWAITTIME");
            if (StringUtils.isEmpty(zlcwaittimeStr)){
                zlcwaittime = 30;
            }else {
                zlcwaittime = Integer.parseInt(zlcwaittimeStr);
            }

            //查询所有ck
            List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
            jdService.plantShareHelp(cks,zlcwaittime);
            return Result.success("东东萌宠互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东萌宠互助异常");
        }
    }

    /**
     * 重置火爆状态
     */
    @LoginToken
    @GetMapping("/resetHot")
    public Result resetHot(@RequestParam Integer type){
        jdService.resetHot(type);
        return Result.success();
    }


    /**
     * 维护助力码
     */
    @GetMapping("/updateShareCode")
    public Result updateShareCode(){
        try {
            List<JdCkEntity> cks = jdService.queryCksAndActivity();
            log.info("开始维护东东农场互助码...");
            jdService.updateFruitShareCode(cks);
            log.info("开始维护东东萌宠互助码...");
            jdService.updatePetShareCode(cks);
            log.info("开始维护种豆得豆互助码...");
            jdService.updatePlantShareCode(cks);
            return Result.success("维护助力码开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","维护助力码异常");
        }
    }

    /**
     * 互助
     */
    @GetMapping("/shareHelp")
    public Result shareHelp(){
        try {
            //接口调用等待时间
            int zlcwaittime = 0;
            String zlcwaittimeStr = systemParamUtil.querySystemParam("ZLCWAITTIME");
            if (StringUtils.isEmpty(zlcwaittimeStr)){
                zlcwaittime = 10;
            }else {
                zlcwaittime = Integer.parseInt(zlcwaittimeStr);
            }
            //查询所有ck
            List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();

            //东东农场
            jdService.fruitShareHelp(cks,zlcwaittime);

            //东东萌宠
            jdService.petShareHelp(cks, zlcwaittime);

            //种豆得豆
            jdService.plantShareHelp(cks,zlcwaittime);
            return Result.success("互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","互助异常");
        }
    }

    /**
     * 获取助力信息
     */
    @GetMapping("/getJdInfo")
    public Result getJdInfo(){
        JSONObject helpInfo = jdService.getJdInfo();
        return Result.success(helpInfo);
    }

    /**
     * 获取助力池信息
     */
    @LoginToken
    @GetMapping("/getZlcInfo")
    public Result getZlcInfo(){
        JSONObject zlcInfo = jdService.getZlcInfo();
        return Result.success(zlcInfo);
    }

    /**
     * 获取助力清单
     */
    @PassToken
    @GetMapping("/getHelpList")
    public Result getHelpList(@RequestParam String type){
        log.info("获取助力清单入参:{}",type);
        List helpList = jdService.getHelpList(type);
        Result success = Result.success(helpList);
        log.info("获取助力清单出参:{}",JSONObject.toJSONString(helpList));
        return success;
    }

    /**
     * 統計京豆
     */
    @GetMapping("/countJd")
    public Result countJd(){
        jdService.countJd();
        return Result.success();
    }

}
