package com.bienao.robot.mapper.jingdong;

import com.bienao.robot.entity.jingdong.JdFruitEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JdFruitMapper {

    /**
     * 重置ck的助力数据
     * @return
     */
    int resetFruitStatus();

    /**
     * 修改东东农场数据
     * @param jdFruitEntity
     * @return
     */
    int updateJdFruit(JdFruitEntity jdFruitEntity);

    /**
     * 获取农场已助力满人数
     * @return
     */
    int getHelpTimes();

    /**
     * 获取农场抽奖已助力满人数
     * @return
     */
    int getHelpLotteryTimes();

    /**
     * 添加东东农场
     * @param jdFruitEntity
     * @return
     */
    int addJdFruit(JdFruitEntity jdFruitEntity);

    void clear();
}
