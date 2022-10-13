package com.bienao.robot.service.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdCkEntity;

import java.util.List;

public interface CkService {

    /**
     * 查询当前ck信息
     * @param ck
     * @return
     */
    JSONObject queryDetail(String ck);

    /**
     * 添加ck
     * @param jdCkEntity
     * @return
     */
    boolean addCk(JdCkEntity jdCkEntity);

    boolean addCkWithOutCheck(JdCkEntity jdCkEntity);

    /**
     * 每天凌晨重置ck的助力数据
     */
    void resetCkStatus();

    /**
     * 检查ck是否过期
     */
    void checkCk();

    Result getJdCks(String ck,String ptPin,Integer level,Integer status,String remark,Integer pageNo,Integer pageSize);

    Result updateJdCk(JdCkEntity jdCkEntity);

    Result deleteJdCks(List<Integer> ids);

    Result getJdCkList(String ck, String ptPin, Integer status,String qlName,Integer pageNo,Integer pageSize);

    List<JdCkEntity> getJdCkListWithoutPage();

    Result enableJdCk(List<JSONObject> cks);

    Result disableJdCk(List<JSONObject> cks);

    Result deleteJdCk(List<JSONObject> cks);
}
