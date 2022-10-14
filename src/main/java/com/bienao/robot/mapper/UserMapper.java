package com.bienao.robot.mapper;

import com.bienao.robot.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 添加用户
     * @param user
     * @return
     */
    int addUser(User user);

    /**
     * 查询用户
     * @param user
     * @return
     */
    User queryUser(User user);

    /**
     * 修改用户
     * @param user
     * @return
     */
    int updateUser(User user);

    /**
     * 根据功能类型查询
     * @param functionType
     * @return
     */
    List<User> queryUserByFunctionType(Integer functionType);

    List<User> queryUsers(User userQuery);
}
