package com.bienao.robot.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class PageUtil {

    public static JSONObject page(List list,Integer pageNo,Integer pageSize){
        int start = cn.hutool.core.util.PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = cn.hutool.core.util.PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", list.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        list = list.subList(start, Math.min(end, list.size()));
        result.put("list", list);
        return result;
    }
}
