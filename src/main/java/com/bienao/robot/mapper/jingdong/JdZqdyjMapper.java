package com.bienao.robot.mapper.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdPetEntity;
import com.bienao.robot.entity.jingdong.JdZqdyjEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JdZqdyjMapper {

    /**
     * 重置ck的助力数据
     * @return
     */
    int reset();

    /**
     * 修改
     * @param jdZqdyjEntity
     * @return
     */
    int update(JdZqdyjEntity jdZqdyjEntity);

    int updateByCkId(JdZqdyjEntity jdZqdyjEntity);

    /**
     * 获取已助力满人数
     * @return
     */
    int getHelpTimes();

    /**
     * 添加
     * @param jdZqdyjEntity
     * @return
     */
    int add(JdZqdyjEntity jdZqdyjEntity);

    void clear();

    List<JSONObject> getZqdyjCk();

    List<JdZqdyjEntity> getHelpList();

    int resetHot();

    JdZqdyjEntity getJdZqdyjByPtPin(String ptPin);

    JdZqdyjEntity getJdZqdyjByHelpCode(String helpCode);

    Integer queryMaxId();
}
