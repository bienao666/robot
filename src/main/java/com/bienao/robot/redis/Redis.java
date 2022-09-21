package com.bienao.robot.redis;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

public class Redis {
    public static Cache<String,String> redis = CacheUtil.newFIFOCache(100);
    public static Cache<String,String> wireRedis = CacheUtil.newFIFOCache(100);
}
