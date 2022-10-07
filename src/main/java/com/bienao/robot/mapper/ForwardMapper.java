package com.bienao.robot.mapper;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.ForwardEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ForwardMapper {

    /**
     * 添加转发
     * @param
     * @return
     */
    int addForward(String from,String fromName,Integer fromtype ,String to,String toName,Integer totype);

    /**
     * 查询转发
     *
     * @return
     */
    List<ForwardEntity> queryForward(String from, String fromName, String to, String toName);

    /**
     * 删除
     * @param ids
     */
    void deleteForward(List<Integer> ids);
}
