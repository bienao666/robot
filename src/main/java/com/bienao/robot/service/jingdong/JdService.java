package com.bienao.robot.service.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;

import java.util.List;

public interface JdService {
    /**
     * 互助
     */
    void shareHelp(boolean reset);

    /**
     * 东东农场互助
     */
    void fruitShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 东东农场天天抽奖互助
     */
    void fruitLotteryShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 东东萌宠互助
     */
    void petShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 种豆得豆互助
     */
    void plantShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 维护助力码
     */
    void updateShareCode();

    /**
     * 获取助力信息
     */
    JSONObject getJdInfo();

    /**
     * 统计京豆收益
     */
    void countJd();
}
