package com.bienao.robot.service.jingdong;

import com.alibaba.fastjson.JSONObject;

public interface CkService {

    /**
     * 查询当前ck信息
     * @param ck
     * @return
     */
    JSONObject queryDetail(String ck);

    /**
     * 添加ck
     * @param ck
     * @param level
     * @return
     */
    boolean addCk(String ck,int level);

    /**
     * 每天凌晨重置ck的助力数据
     */
    void resetCkStatus();

    /**
     * 检查ck是否过期
     */
    void checkCk();
}
