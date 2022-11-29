package com.bienao.robot.redis;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

public class Redis {
    public static TimedCache<String,String> redis;
    public static TimedCache<String,String> wireRedis;

    static {
        redis = CacheUtil.newTimedCache(100);
        redis.schedulePrune(1000);
        wireRedis = CacheUtil.newTimedCache(100);
        wireRedis.schedulePrune(1000);
    }

}
