package com.bienao.robot.controller.token;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.entity.User;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.token.TokenService;
import com.bienao.robot.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/token")
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public Result login(String username, String password) {
        User user = userService.getUser(username, password);

        if (user == null) {
            return Result.error("message", "登录失败！");
        } else {
            String token = tokenService.getToken(user);
            JSONObject result = new JSONObject();
            result.put("token",token);
            return Result.success(result);
        }
    }

    @LoginToken
    @PostMapping("/getMessage")
    public Result getMessage() {
        return Result.success("您已通过验证");
    }
}
