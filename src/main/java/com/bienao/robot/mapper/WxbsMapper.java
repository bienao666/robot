package com.bienao.robot.mapper;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Wxbs;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WxbsMapper {

    /**
     * 添加微信刷步数
     * @param
     * @return
     */
    int addZh(String username,String password,Integer minstep,Integer maxstep,String expiryTime);

    /**
     * 删除
     * @param id
     */
    void delete(Integer id);

    List<Wxbs> queryZhs();
}
