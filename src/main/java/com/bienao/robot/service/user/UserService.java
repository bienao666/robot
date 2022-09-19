package com.bienao.robot.service.user;

import com.bienao.robot.entity.User;
import com.bienao.robot.entity.Result;

public interface UserService {

    User getUser(String username, String password);

    User getUser(String username);

    Result check();

    Result register(String username, String password);
}
