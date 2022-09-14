package com.bienao.robot.service.user;

import com.bienao.robot.entity.User;

public interface UserService {

    User getUser(String username, String password);

    User getUser(String username);
}
