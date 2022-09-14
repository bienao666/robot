package com.bienao.robot.controller.token;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.User;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.token.TokenService;
import com.bienao.robot.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PassToken
    @PostMapping("/login")
    public Result login(@RequestBody JSONObject jsonObject) {
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
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

    @PassToken
    @GetMapping("/check")
    public Result check() {
        return userService.check();
    }

    @PassToken
    @PostMapping("/register")
    public Result register(@RequestBody JSONObject jsonObject) {
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        if (StringUtils.isEmpty(username)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"用户名不能为空");
        }
        if (StringUtils.isEmpty(password)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"密码不能为空");
        }
        return userService.register(username, password);
    }

    @PassToken
    @PostMapping("/getMessage")
    public Result getMessage() {
        return Result.success("您已通过验证");
    }
}
