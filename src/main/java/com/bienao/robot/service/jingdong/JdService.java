package com.bienao.robot.service.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;

import java.util.List;

public interface JdService {

    /**
     * 东东农场互助
     */
    void fruitShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 东东农场天天抽奖互助
     */
//    void fruitLotteryShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 东东萌宠互助
     */
    void petShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 种豆得豆互助
     */
    void plantShareHelp(List<JdCkEntity> cks,int zlcwaittime);

    /**
     * 查询所有ck
     */
    List<JdCkEntity> queryCksAndActivity();

    /**
     * 获取助力信息
     */
    JSONObject getJdInfo();

    /**
     * 统计京豆收益
     */
    void countJd();

    void updateFruitShareCode(List<JdCkEntity> cks);

    void updatePetShareCode(List<JdCkEntity> cks);

    void updatePlantShareCode(List<JdCkEntity> cks);

    void qlToZlc();

    void addJdck(String ck,String remarks, Integer status,int count,String qlRemark);

    List getHelpList(String type);

    void clear();

    JSONObject getZlcInfo();
}
