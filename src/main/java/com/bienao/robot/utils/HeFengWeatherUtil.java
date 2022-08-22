package com.bienao.robot.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 和风天气util
 */
@Component
public class HeFengWeatherUtil {
    /**
     * 获取城市信息
     *
     * @param name 城市名称
     * @param adm  城市的上级行政区划
     * @return
     */
    public JSONObject getCity(String name, String adm, String key) {
        String url = "https://geoapi.qweather.com/v2/city/lookup?location=" + name + "&adm=" + adm + "&key=" + key;
        String res = HttpUtil.get(url);
        System.out.println(res);
        JSONObject jsonObject = JSONObject.parseObject(res);
        if (jsonObject.getInteger("code") == 200) {
            JSONArray location = jsonObject.getJSONArray("location");
            if (location.size() > 0) {
                return location.getJSONObject(0);
            } else {
                throw new RuntimeException("城市错误");
            }
        } else {
            throw new RuntimeException("城市错误");
        }
    }

    /**
     * 获取城市当前天气
     *
     * @param city 城市code
     * @return
     */
    public JSONObject now(Integer city, String key) {
        String url = "https://devapi.qweather.com/v7/weather/now?location=" + city + "&key=" + key;
        String res = HttpUtil.get(url);
        JSONObject jsonObject = JSONObject.parseObject(res);
        if (jsonObject.getInteger("code") != 200) {
            throw new RuntimeException("获取城市天气失败");
        }
        return jsonObject.getJSONObject("now");
    }

    /**
     * 城市当前预警
     *
     * @return
     */
    public JSONObject warningNow(Integer cityCode, String key) {
        String url = "https://devapi.qweather.com/v7/warning/now?location=" + cityCode + "&key=" + key;
        String res = HttpUtil.get(url);
        JSONObject jsonObject = JSONObject.parseObject(res);
        if (jsonObject.getInteger("code") != 200) {
            throw new RuntimeException("获取城市天气失败");
        }
        if (CollectionUtils.isEmpty(jsonObject.getJSONArray("warning"))) {
            return null;
        }
        return jsonObject.getJSONArray("warning").getJSONObject(0);
    }
}
