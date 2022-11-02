package com.bienao.robot.mapper.jingdong;

import com.bienao.robot.entity.jingdong.JdPetEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JdZqdyjMapper {

    /**
     * 重置ck的助力数据
     * @return
     */
    int resetStatus();

    /**
     * 修改
     * @param jdPetEntity
     * @return
     */
    int update(JdPetEntity jdPetEntity);

    /**
     * 获取已助力满人数
     * @return
     */
    int getHelpTimes();

    /**
     * 添加
     * @param jdPetEntity
     * @return
     */
    int add(JdPetEntity jdPetEntity);

    void clear();
}
