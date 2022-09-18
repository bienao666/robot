package com.bienao.robot.service.impl.ylgy;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.mapper.YlgyMapper;
import com.bienao.robot.service.ylgy.YlgyService;
import com.bienao.robot.utils.YlgyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class YlgyServiceImpl implements YlgyService {

    @Autowired
    private YlgyMapper ylgyMapper;

    //同时刷号个数上限
    private Integer limit = 3;
    //当前刷号个数
    private Integer count = 0;

    @Override
    public void brush() {
        if (count<limit){
            //查询2个待刷账号
            List<JSONObject> list = ylgyMapper.query();
            for (JSONObject ylgy : list) {
                if (count<=limit){
                    count++;
                    log.info("开始刷羊了个羊uid：{}",ylgy.getString("uid"));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String id = ylgy.getString("id");
                            String token = ylgy.getString("token");
                            String timeStr = ylgy.getString("times");
                            if (StringUtils.isEmpty(timeStr)){
                                timeStr="100000";
                            }
                            Integer times = Integer.parseInt(timeStr);
                            Integer time = 0;
                            while (true){
                                if (time>times){
                                    ylgyMapper.delete(id);
                                    count--;
                                    return;
                                }
                                try {
                                    String resStr = HttpRequest.get("https://cat-match.easygame2021.com/sheep/v1/game/game_over?t="+token+"&rank_score=1&rank_state=1&rank_time=20&rank_role=1&skin=9")
                                            .header("Host","cat-match.easygame2021.com")
                                            .header("Connection","keep-alive")
                                            .header("t",token)
                                            .header("content-type","application/json")
                                            .header("Accept-Encoding","gzip,compress,br,deflate")
                                            .header("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 15_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.27(0x18001b36) NetType/4G Language/zh_CN")
                                            .header("Referer","https://servicewechat.com/wx141bfb9b73c970a9/14/page-frame.html")
                                            .timeout(3000)
                                            .execute().body();
//                                    log.info("羊了个羊代刷：{}",resStr);
                                    if (StringUtils.isNotEmpty(resStr)){
                                        JSONObject res = JSONObject.parseObject(resStr);
                                        if (0==res.getInteger("err_code")){
                                            time++;
                                        }
                                        if (10003==res.getInteger("err_code")){
                                            token = YlgyUtils.getYlgyToken(ylgy.getString("uid"));
                                        }
                                    }
                                } catch (Exception e) {
                                    log.info("羊了个羊代刷异常：",e);
                                }
                            }
                        }
                    }).start();
                }
            }
        }
    }
}
