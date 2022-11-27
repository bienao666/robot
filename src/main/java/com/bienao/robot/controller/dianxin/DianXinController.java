package com.bienao.robot.controller.dianxin;


import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.service.dianxin.DianXinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 电信
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/dx")
public class DianXinController {

    @Autowired
    private DianXinService dianXinService;

    /**
     * 电信直播间抽奖
     *
     * @return
     */
    @PassToken
    @GetMapping("/lotteryLive")
    public Result lotteryLive(@RequestParam String phone ,@RequestParam String passWord) {
        return Result.success();
    }
}
