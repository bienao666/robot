package com.bienao.robot.controller.weixin;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/message")
    public JSONObject message(@RequestBody JSONObject jsonObject) {
        log.info("接收消息：{}", jsonObject.toJSONString());
        EvictingQueue<JSONObject> messageLists = WXConstant.messageList;
        messageLists.add(jsonObject);
        JSONObject result = new JSONObject();
        result.put("Code", "0");
        return result;
    }
}
