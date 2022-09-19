package com.bienao.robot.mapper;

import com.bienao.robot.entity.WireKeyEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WireKeyMapper{

    /**
     * 添加活动洞察变量
     * @param wireKeyEntities
     * @return
     */
//    int addWireKey(WireKeyEntity wireKeyEntities);

    /**
     * 添加活动洞察变量
     * @param wireId
     * @param key
     * @return
     */
    int addWireKey(Integer wireId,String key);

    /**
     * 修改活动洞察变量
     * @param keyEntity
     * @return
     */
    int updateWireKey(WireKeyEntity keyEntity);

    /**
     * 删除活动洞察变量
     * @param ids
     * @return
     */
    int deleteWireKey(List<Integer> ids);

    /**
     * 根据
     * @param key
     * @return
     */
    String queryScript(String key);
}
