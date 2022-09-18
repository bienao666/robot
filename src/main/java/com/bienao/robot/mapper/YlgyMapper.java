package com.bienao.robot.mapper;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.WireEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YlgyMapper {

    /**
     * 添加羊了个羊
     * @param
     * @return
     */
    int addWire(String uid,String token,Integer times);

    /**
     * 查询总数
     *
     * @return
     */
    Integer queryCount();

    /**
     * 查询待刷号
     *
     * @return
     */
    List<JSONObject> query();

    /**
     * 删除
     * @param id
     */
    void delete(String id);
}
