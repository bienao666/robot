package com.bienao.robot.controller.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.jingdong.JdService;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 东东农场互助
     */
    @GetMapping("/fruitShareHelp")
    public Result fruitShareHelp(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //查询所有ck
                    List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
                    jdService.fruitShareHelp(cks,10);
                }
            }).start();
            return Result.success("东东农场互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东农场互助异常");
        }
    }

    /**
     * 东东农场天天抽奖互助
     */
    @GetMapping("/fruitLotteryShareHelp")
    public Result fruitLotteryShareHelp(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //查询所有ck
                    List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
                    jdService.fruitLotteryShareHelp(cks,10);
                }
            }).start();
            return Result.success("东东农场天天抽奖互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东农场天天抽奖互助异常");
        }
    }

    /**
     * 东东萌宠互助
     */
    @GetMapping("/petShareHelp")
    public Result petShareHelp(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //查询所有ck
                    List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
                    jdService.petShareHelp(cks,10);
                }
            }).start();
            return Result.success("东东萌宠互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东萌宠互助异常");
        }
    }

    /**
     * 种豆得豆互助
     */
    @GetMapping("/plantShareHelp")
    public Result plantShareHelp(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //查询所有ck
                    List<JdCkEntity> cks = jdCkMapper.queryCksAndActivity();
                    jdService.plantShareHelp(cks,10);
                }
            }).start();
            return Result.success("东东萌宠互助开始");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1","东东萌宠互助异常");
        }
    }

    /**
     * 维护助力码
     */
    @GetMapping("/updateShareCode")
    public Result updateShareCode(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    jdService.updateShareCode();
                }
            }).start();
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    jdService.shareHelp(false);
                }
            }).start();
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



}
