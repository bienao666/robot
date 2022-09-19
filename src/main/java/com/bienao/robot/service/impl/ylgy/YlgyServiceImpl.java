package com.bienao.robot.service.impl.ylgy;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.mapper.YlgyMapper;
import com.bienao.robot.service.ylgy.YlgyService;
import com.bienao.robot.utils.YlgyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class YlgyServiceImpl implements YlgyService {

    @Autowired
    private YlgyMapper ylgyMapper;

    //正在刷的序号
    private ArrayList<String> idList = new ArrayList<>();

    @Override
    public List<JSONObject> query() {
        return ylgyMapper.query();
    }

    /**
     * 开刷
     *
     * @param id
     * @param token
     * @param times
     */
    @Async("asyncServiceExecutor")
    public void handleBrush(String id, String uid, String token, Integer times) {
        if (idList.contains(id)) {
            return;
        }
        idList.add(id);
        Integer time = 0;
        while (true) {
            if (time > times) {
                ylgyMapper.delete(id);
                idList.remove(id);
                return;
            }
            try {
                String resStr = HttpRequest.get("https://cat-match.easygame2021.com/sheep/v1/game/game_over?t=" + token + "&rank_score=1&rank_state=1&rank_time=20&rank_role=1&skin=9")
                        .header("Host", "cat-match.easygame2021.com")
                        .header("Connection", "keep-alive")
                        .header("t", token)
                        .header("content-type", "application/json")
                        .header("Accept-Encoding", "gzip,compress,br,deflate")
                        .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 15_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.27(0x18001b36) NetType/4G Language/zh_CN")
                        .header("Referer", "https://servicewechat.com/wx141bfb9b73c970a9/14/page-frame.html")
                        .timeout(3000)
                        .execute().body();
//                                    log.info("羊了个羊代刷：{}",resStr);
                if (StringUtils.isNotEmpty(resStr)) {
                    JSONObject res = JSONObject.parseObject(resStr);
                    if (0 == res.getInteger("err_code")) {
                        time++;
                    }
                    if (10003 == res.getInteger("err_code")) {
                        token = YlgyUtils.getYlgyToken(uid);
                    }
                }
                int i = RandomUtil.randomInt(10);
                if (i == 5) {
                    log.info("用户 {} 羊了个羊第 {} 次刷关随机日志：{}", uid, time, resStr);
                }
                Thread.sleep(i * 100);
            } catch (Exception e) {
                log.info("羊了个羊代刷异常：", e);
            }
        }
    }
}
