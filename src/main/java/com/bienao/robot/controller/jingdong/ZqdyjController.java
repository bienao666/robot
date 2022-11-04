package com.bienao.robot.controller.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.Result;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.service.jingdong.ZqdyjService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 赚钱大赢家
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/zqdyj")
public class ZqdyjController {

    @Autowired
    private ZqdyjService zqdyjService;

    /**
     * 赚钱大赢家助力
     */
    @PassToken
    @PostMapping("/help")
    public Result help(@RequestBody JSONObject jsonObject){
        String param = jsonObject.getString("param");
        if (StringUtils.isEmpty(param)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ck或者助力码不能为空");
        }

        return zqdyjService.help(param);
    }
}
