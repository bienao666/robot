package com.bienao.robot.controller;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.User;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.redis.Redis;
import com.bienao.robot.utils.jingdong.JdBeanChangeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JdCkMapper jdCkMapper;

    @Autowired
    private JdBeanChangeUtil jdBeanChangeUtil;

    @GetMapping("/test")
    public void test() {
        String jdPtPins = "#jd_6df829e3c988b#jd_eiVOWzxnKISM";
        String[] split = jdPtPins.split("#");
        for (String jdPtPin : split) {
            if (StringUtils.isEmpty(jdPtPin)){
                continue;
            }
            JdCkEntity jdCkEntityQuery = new JdCkEntity();
            jdCkEntityQuery.setPtPin(jdPtPin);
            JdCkEntity jdCkEntity = jdCkMapper.queryCk(jdCkEntityQuery);
            String jdBeanChange = null;
            try {
                jdBeanChange = jdBeanChangeUtil.getJdBeanChange(jdCkEntity);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
