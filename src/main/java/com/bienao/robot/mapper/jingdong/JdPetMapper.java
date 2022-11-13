package com.bienao.robot.mapper.jingdong;

import com.bienao.robot.entity.jingdong.JdFruitEntity;
import com.bienao.robot.entity.jingdong.JdPetEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JdPetMapper {

    /**
     * 重置ck的助力数据
     * @return
     */
    int resetPetStatus();

    int resetHot();

    /**
     * 修改东东萌宠数据
     * @param jdPetEntity
     * @return
     */
    int updateJdPet(JdPetEntity jdPetEntity);

    /**
     * 获取萌宠已助力满人数
     * @return
     */
    int getHelpTimes();

    /**
     * 添加东东萌宠
     * @param jdPetEntity
     * @return
     */
    int addJdPet(JdPetEntity jdPetEntity);

    void clear();

    List<JdPetEntity> getJdPets();
}
