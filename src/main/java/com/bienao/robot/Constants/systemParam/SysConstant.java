package com.bienao.robot.Constants.systemParam;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

public class SysConstant {
    /**
     * 系统参数缓存
     */
    public static Cache<String,String> sysParamRedis =  CacheUtil.newFIFOCache(500);
}
