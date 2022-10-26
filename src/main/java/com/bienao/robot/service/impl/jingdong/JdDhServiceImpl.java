package com.bienao.robot.service.impl.jingdong;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.PatternConstant;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.service.jingdong.JdDhService;
import com.bienao.robot.utils.jingdong.GetUserAgentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

/**
 * 健康社区兑换京豆
 */
@Component
@Slf4j
public class JdDhServiceImpl implements JdDhService {

    @Override
    @Async("asyncServiceExecutor")
    public void jkExchange(List<JdCkEntity> cks){
        for (JdCkEntity jdCkEntity : cks) {
            String ck = jdCkEntity.getCk();
            Matcher matcher = PatternConstant.ckPattern.matcher(ck);
            if (matcher.find()) {
                String userName = matcher.group(2);
                log.info("******开始健康社区兑换京豆【京东账号：{}】*********",userName);
                JSONObject body = new JSONObject();
                body.put("commodityType",2);
                body.put("commodityId",4);
                String resStr = HttpRequest.post("https://api.m.jd.com")
                        .header("content-type", "application/x-www-form-urlencoded")
                        .header("User-Agent", GetUserAgentUtil.getUserAgent())
                        .header("Cookie", ck)
                        .body("functionId=jdhealth_exchange&body=" + body.toJSONString() + "&client=wh5&clientVersion=1.0.0&uuid=")
                        .timeout(500)
                        .execute().body();
                log.info("健康社区兑换返回：{}",resStr);
                if (StringUtils.isNotEmpty(resStr)){
                    JSONObject res = JSONObject.parseObject(resStr);
                    JSONObject data = res.getJSONObject("data");
                    if ("success".equals(data.getString("bizMsg"))){
                        log.info("兑换成功:{}豆",data.getJSONObject("result").getInteger("jingBeanNum"));
                    }else if ("到达今日兑换次数上限，不能再兑换哦~".equals(data.getString("bizMsg"))){
                        log.info("今日兑换次数上限");
                    }else if("活动太火爆啦".equals(data.getString("bizMsg"))){
                        log.info("活动太火爆啦");
                    }else if ("来晚啦，已被抢光了哦~".equals(data.getString("bizMsg"))){
                        break;
                    }else {
                        log.info("兑换失败：{}",data.getString("bizMsg"));
                    }
                }

                //休息0.5秒
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
