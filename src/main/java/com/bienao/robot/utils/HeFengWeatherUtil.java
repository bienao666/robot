package com.bienao.robot.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;

/**
 * 和风天气util
 */
public class HeFengWeatherUtil {
    /**
     * 秘钥
     */
    public static final String SECRET_KEY = "cabf568dafb443df917484ba0b631a6a";

    /**
     * 获取城市信息
     *
     * @param name 城市名称
     * @param adm  城市的上级行政区划
     * @return
     */
    public static JSONObject getCity(String name, String adm) {
        String url = "https://geoapi.qweather.com/v2/city/lookup?location=" + name + "&adm=" + adm + "&key=" + SECRET_KEY;
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
    public static JSONObject now(Integer city) {
        String url = "https://devapi.qweather.com/v7/weather/now?location=" + city + "&key=" + SECRET_KEY;
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
    public static JSONObject warningNow(Integer cityCode) {
        String url = "https://devapi.qweather.com/v7/warning/now?location=" + cityCode + "&key=" + SECRET_KEY;
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
