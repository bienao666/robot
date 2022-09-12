package com.bienao.robot.controller.telegram;

import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.telegram.TgService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * tg接口
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/tg")
public class TgController {

    @Autowired
    private TgService tgService;

    @Autowired
    private SystemParamUtil systemParamUtil;

    /**
     * 启动tgbot
     */
    /*@GetMapping("/startTg")
    public Result startTg(){
        List<SystemParam> SystemParams = systemParamUtil.querySystemParams("TGPROXY");
        SystemParam tgProxy = SystemParams.get(0);
        SystemParams = systemParamUtil.querySystemParams("TGBOTTOKEN");
        SystemParam tgbotToken = SystemParams.get(0);
        SystemParams = systemParamUtil.querySystemParams("TGBOTUSERNAME");
        SystemParam tgbotUserName = SystemParams.get(0);
        return tgService.startTg(tgProxy.getValue(),tgbotToken.getValue(),tgbotUserName.getValue());
    }*/
}
