package com.bienao.robot.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WxbsMapper {

    /**
     * 添加羊了个羊
     * @param
     * @return
     */
    int addZh(String username,String password,Integer minstep,Integer maxstep,String expiryTime);

    /**
     * 删除
     * @param id
     */
    void delete(String id);
}
