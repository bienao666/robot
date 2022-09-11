package com.bienao.robot.mapper;

import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.WireEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WireMapper {

    /**
     * 添加活动
     * @param wireEntity
     * @return
     */
    int addWire(WireEntity wireEntity);

    /**
     * 查询自增主键ID
     *
     * @return
     */
    Integer queryMaxId();

    /**
     * 查询活动个数
     *
     * @return
     */
    Integer queryCount();

    /**
     * 查询活动
     * @param key
     * @return
     */
    List<WireEntity> queryWire(String key);

    /**
     * 修改线报活动
     * @param wireEntity
     * @return
     */
    Integer updateWire(WireEntity wireEntity);

    /**
     * 删除线报活动
     * @param ids
     * @return
     */
    Integer deleteWire(List<Integer> ids);

    /**
     * 修改线报脚本状态
     */
    void updateWireStatus(String script,String status);
}
