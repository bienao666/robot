package com.bienao.robot.service.impl.user;

import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.entity.User;
import com.bienao.robot.service.user.UserService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Override
    public User getUser(String username, String password){
        List<SystemParam> systemParams = systemParamUtil.querySystemParams("USERNAME");
        String userName = systemParams.get(0).getValue();
        systemParams = systemParamUtil.querySystemParams("PASSWORD");
        String passWord = systemParams.get(0).getValue();

        if (userName.equals(username) && passWord.equals(password)){
            User user=new User();
            user.setUserName(username);
            user.setPassWord(password);
            return user;
        }
        else{
            return null;
        }
    }

    @Override
    public User getUser(String username){
        List<SystemParam> systemParams = systemParamUtil.querySystemParams("USERNAME");
        String userName = systemParams.get(0).getValue();
        systemParams = systemParamUtil.querySystemParams("PASSWORD");
        String passWord = systemParams.get(0).getValue();
        if (userName.equals(username)){
            User user=new User();
            user.setUserName(userName);
            user.setPassWord(passWord);
            return user;
        }
        else{
            return null;
        }
    }
}
