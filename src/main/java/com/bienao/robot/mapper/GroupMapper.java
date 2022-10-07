package com.bienao.robot.mapper;

import com.bienao.robot.entity.Group;
import com.bienao.robot.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupMapper {

    /**
     * 添加用户
     * @param group
     * @return
     */
    int addGroup(Group group);

    /**
     * 根据功能类型查询
     * @param functionType
     * @return
     */
    List<Group> queryGroupByFunctionType(Integer functionType);

    /**
     * 根据群号和功能类型查询
     * @param functionType
     * @return
     */
    Group queryGroupByGroupIdAndFunctionType(String groupid,Integer functionType);

    /**
     * 根据群号和功能类型删除
     * @param functionType
     * @return
     */
    int deleteGroupByGroupIdAndFunctionType(String groupid,Integer functionType);

    /**
     * 根据群号查询群
     * @param groupid
     * @return
     */
    List<Group> queryGroupByGroupId(String groupid);
}
