package com.bienao.robot.service.impl.ql;

import cn.hutool.cache.Cache;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.utils.ql.QlUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class QlServiceImpl implements QlService {

    @Autowired
    private QlUtil qlUtil;

    private Cache<String, String> redis = WXConstant.redis;

    /**
     * 一键设置大车头
     * @return
     */
    @Override
    public boolean oneKeyBigHead(JSONObject content) {
        JSONObject qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
        String bigHead = qlgl.getString("bigHead");

        return true;
    }
}
