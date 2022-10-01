package com.bienao.robot.Constants.weixin;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

public class WXConstant {
    /**
     * 缓存
     */
    public static Cache<String,String> redis =  CacheUtil.newFIFOCache(500);
}
