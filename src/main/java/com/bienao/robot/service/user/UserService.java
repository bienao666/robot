package com.bienao.robot.service.user;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.User;
import com.bienao.robot.entity.Result;

public interface UserService {

    User getUser(String username, String password);

    User getUser(String username);

    Result check();

    Result register(String username, String password);

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

    void saveUser(JSONObject content, String from_wxid, String ptPin, String wxpusherUid);
}
