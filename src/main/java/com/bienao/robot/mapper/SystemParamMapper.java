package com.bienao.robot.mapper;

import com.bienao.robot.entity.SystemParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SystemParamMapper {

    /**
     * 查询参数
     * @return
     */
    List<SystemParam> querySystems(String code,String codeName);

    /**
     * 查询参数
     * @return
     */
    SystemParam querySystem(String code);

    /**
     * 添加参数
     * @param systemParam
     * @return
     */
    int addSystemParam(SystemParam systemParam);

    /**
     * 更新参数
     * @param systemParam
     * @return
     */
    int updateSystemParam(SystemParam systemParam);

    /**
     * 删除参数
     * @param ids
     * @return
     */
    int deleteSystemParams(List<Integer> ids);
}
