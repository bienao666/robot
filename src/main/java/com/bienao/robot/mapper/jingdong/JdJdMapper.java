package com.bienao.robot.mapper.jingdong;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JdJdMapper {

    //查询近七天的京豆数据
    List<JSONObject> queryJd();

    //更新京豆数据
    int addJdDate(String date, int jdCount);
}
