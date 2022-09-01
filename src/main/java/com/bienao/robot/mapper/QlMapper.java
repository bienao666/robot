package com.bienao.robot.mapper;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QlMapper {

    /**
     * 添加青龙
     * @param ql
     * @return
     */
    int addQl(JSONObject ql);

    /**
     * 查询青龙
     * @param id
     * @return
     */
    JSONObject queryQl(String id);

    /**
     * 查询青龙
     * @param url
     * @return
     */
    JSONObject queryQlByUrl(String url);

    /**
     * 查询青龙
     * @return
     */
    List<JSONObject> queryQls();

    /**
     * 修改青龙
     * @param ql
     * @return
     */
    int updateQl(JSONObject ql);

}
