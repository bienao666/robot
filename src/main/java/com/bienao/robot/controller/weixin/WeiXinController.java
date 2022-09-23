package com.bienao.robot.controller.weixin;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.Result;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.service.weixin.WxService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信接口
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/weixin")
public class WeiXinController {

    @Autowired
    private WxService wxService;

    @PostMapping("/message")
    @PassToken
    public JSONObject message(@RequestBody JSONObject jsonObject) {
        log.info("接收消息：{}", jsonObject.toJSONString());
        wxService.handleMessage(jsonObject);
        JSONObject result = new JSONObject();
        result.put("Code", "0");
        return result;
    }

    /**
     * 修改微信步数
     * @param jsonObject
     * @return
     */
    @PostMapping("/updateStep")
    @PassToken
    public Result updateStep(@RequestBody JSONObject jsonObject) {
        String username = jsonObject.getString("username");
        if (StringUtils.isEmpty(username)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"账号不能为空");
        }
        String password = jsonObject.getString("password");
        if (StringUtils.isEmpty(password)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"密码不能为空");
        }
        Integer step = jsonObject.getInteger("step");
        if (step==null || step<0 || step>100000){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"步数异常，请填写0-100000之内");
        }
        boolean istime = jsonObject.getBoolean("istime");
        Integer minstep = jsonObject.getInteger("minstep");
        Integer maxstep = jsonObject.getInteger("maxstep");
        String expiryTime = jsonObject.getString("expiryTime");

        return wxService.updateStep(username,password,step,istime,minstep,maxstep,expiryTime);
    }

    /**
     * 修改微信步数(数据库全部账号)
     * @return
     */
    @GetMapping("/updateSteps")
    @PassToken
    public Result updateSteps() {
        wxService.timeWxbs();
        return Result.success();
    }
}
