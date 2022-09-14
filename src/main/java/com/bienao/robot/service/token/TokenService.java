package com.bienao.robot.service.token;

import com.bienao.robot.entity.User;

public interface TokenService {

    String getToken(User user);
}
