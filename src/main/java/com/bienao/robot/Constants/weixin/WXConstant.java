package com.bienao.robot.Constants.weixin;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.EvictingQueue;

public class WXConstant {
    /**
     * 微信消息队列
     */
    public static EvictingQueue<JSONObject> messageList = EvictingQueue.create(100);

    /**
     * 缓存
     */
    public static Cache<String,String> redis =  CacheUtil.newFIFOCache(500);
}
