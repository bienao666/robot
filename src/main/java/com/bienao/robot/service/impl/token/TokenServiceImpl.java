package com.bienao.robot.service.impl.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.bienao.robot.entity.User;
import com.bienao.robot.service.token.TokenService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {
    //5分钟之后过期
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    @Override
    public String getToken(User user) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);

        String token = JWT.create().withAudience(user.getUserName())
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(user.getPassWord()));
        return token;
    }
}

