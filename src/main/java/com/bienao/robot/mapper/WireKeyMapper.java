package com.bienao.robot.mapper;

import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.WireKeyEntity;
import com.bienao.robot.result.Result;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WireKeyMapper{

    /**
     * 添加活动洞察变量
     * @param wireKeyEntities
     * @return
     */
    int addWireKey(List<WireKeyEntity> wireKeyEntities);

    /**
     * 修改活动洞察变量
     * @param keyEntity
     * @return
     */
    int updateWireKey(WireKeyEntity keyEntity);

    /**
     * 删除活动洞察变量
     * @param wireId
     * @return
     */
    int deleteWireKey(Integer wireId);
}
