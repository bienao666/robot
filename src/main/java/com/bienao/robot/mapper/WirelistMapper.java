package com.bienao.robot.mapper;

import com.bienao.robot.entity.WireActivityEntity;
import com.bienao.robot.entity.WireEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WirelistMapper {

    int addActivity(String script,String content);

    List<WireActivityEntity> queryActivity();

}
