package com.bienao.robot.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.User;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.service.user.UserService;
import com.bienao.robot.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户
     * @param wxid
     * @param wxName
     * @param jdPtPin
     * @param status
     * @param level
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PassToken
    @GetMapping("/queryUsers")
    public Result queryUsers(@RequestParam(value = "wxid",required = false) String wxid,
                             @RequestParam(value = "wxName",required = false) String wxName,
                             @RequestParam(value = "jdPtPin",required = false) String jdPtPin,
                             @RequestParam(value = "status",required = false) Integer status,
                             @RequestParam(value = "level",required = false) Integer level,
                             @RequestParam(value = "pageNo") Integer pageNo,
                             @RequestParam(value = "pageSize") Integer pageSize) {
        User userQuery = new User();
        userQuery.setWxid(wxid);
        userQuery.setWxName(wxName);
        userQuery.setJdPtPin(jdPtPin);
        userQuery.setStatus(status);
        userQuery.setLevel(level);
        List<User> users = userService.queryUsers(userQuery);
        JSONObject page = PageUtil.page(users, pageNo, pageSize);
        return Result.success(page);
    }

    /**
     * 修改用户
     * @param user
     * @return
     */
    @PassToken
    @PostMapping("/updateUser")
    public Result queryUsers(@RequestBody User user) {
        int res = userService.updateUser(user);
        if (res==1){
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR, "数据库操作异常");
        }
    }
}
