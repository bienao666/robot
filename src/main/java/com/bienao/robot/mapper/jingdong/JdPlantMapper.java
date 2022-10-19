package com.bienao.robot.mapper.jingdong;

import com.bienao.robot.entity.jingdong.JdPlantEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JdPlantMapper {

    /**
     * 重置ck的助力数据
     * @return
     */
    int resetPlantStatus();

    /**
     * 修改种豆得豆数据
     * @param jdPlantEntity
     * @return
     */
    int updateJdPlant(JdPlantEntity jdPlantEntity);

    /**
     * 获取种豆得豆已助力满人数
     * @return
     */
    int getHelpTimes();

    /**
     * 添加种豆得豆
     * @param jdPlantEntity
     * @return
     */
    int addJdPlant(JdPlantEntity jdPlantEntity);

    void clear();
}
