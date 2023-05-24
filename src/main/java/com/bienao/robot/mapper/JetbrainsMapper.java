package com.bienao.robot.mapper;

import com.bienao.robot.entity.JetbrainsEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description jetbrains
 * @author bienao
 * @date 2023-05-24
 */
@Mapper
public interface JetbrainsMapper {

    /**
     * 新增
     * @author bienao
     * @date 2023/05/24
     **/
    int insert(JetbrainsEntity JetbrainsEntity);

    /**
     * 刪除
     * @author bienao
     * @date 2023/05/24
     **/
    int delete(int id);

    /**
     * 更新
     * @author bienao
     * @date 2023/05/24
     **/
    int update(JetbrainsEntity JetbrainsEntity);

    /**
     * 查询 根据主键 id 查询
     * @author bienao
     * @date 2023/05/24
     **/
    JetbrainsEntity load(int id);

    /**
     * 查询 分页查询
     * @author bienao
     * @date 2023/05/24
     **/
    List<JetbrainsEntity> pageList(int offset, int pagesize);

    List<String> queryAllUrl();

    /**
     * 查询 分页查询 count
     * @author bienao
     * @date 2023/05/24
     **/
    int pageListCount(int offset,int pagesize);

}