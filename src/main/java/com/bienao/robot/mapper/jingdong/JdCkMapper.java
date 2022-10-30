package com.bienao.robot.mapper.jingdong;

import com.bienao.robot.entity.jingdong.JdCkEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JdCkMapper {

    /**
     * 添加ck
     * @param jdCkEntity
     * @return
     */
    int addCk(JdCkEntity jdCkEntity);

    /**
     * 更新ck
     * @param jdCkEntity
     * @return
     */
    int updateCk(JdCkEntity jdCkEntity);

    /**
     * 查询ck
     * @param jdCkEntity
     * @return
     */
    JdCkEntity queryCk(JdCkEntity jdCkEntity);

    /**
     * 批量查询ck
     * @param jdCkEntity
     * @return
     */
    List<JdCkEntity> queryCks(JdCkEntity jdCkEntity);

    /**
     * 批量查询ck
     * @param levels
     * @return
     */
    List<JdCkEntity> queryCksByLevels(String levels);

    /**
     * 东东农场批量查询ck
     * @return
     */
    List<JdCkEntity> queryCksAndActivity();

    /**
     * 删除ck
     * @return
     */
    int deleteCks(List<Integer> ids);

    /**
     * 查询ck数量
     * @param jdCkEntity
     * @return
     */
    int queryCount(JdCkEntity jdCkEntity);

    /**
     * 汇总京豆
     * @return
     */
    int sumJdCount();

    int resetJd();

    /**
     * 查询助力池当前ck数
     * @return
     */
    Integer queryCkCount();

    /**
     * 查询东东农场已助力满的ck
     * @return
     */
    List<JdCkEntity> queryHasHelpFruitCk();

    /**
     * 查询东东萌宠已助力满的ck
     * @return
     */
    List<JdCkEntity> queryHasHelpPetCk();

    /**
     * 查询种豆得豆已助力满的ck
     * @return
     */
    List<JdCkEntity> queryHasHelpPlantCk();

    /**
     * 过期ck
     * @param ids
     * @return
     */
    List<JdCkEntity> queryByIdsCk(List<Integer> ids);
}
