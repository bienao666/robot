package com.bienao.robot.service.impl.user;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.entity.User;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.SystemParamMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.mapper.UserMapper;
import com.bienao.robot.service.user.UserService;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private SystemParamMapper systemParamMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUser(String username, String password){
        SystemParam systemParams = systemParamMapper.querySystem("USERNAME");
        String userName = systemParams.getValue();
        systemParams = systemParamMapper.querySystem("PASSWORD");
        String passWord = systemParams.getValue();

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
        SystemParam systemParams = systemParamMapper.querySystem("USERNAME");
        String userName = systemParams.getValue();
        systemParams = systemParamMapper.querySystem("PASSWORD");
        String passWord = systemParams.getValue();
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

    @Override
    public Result check() {
        SystemParam systemParams = systemParamMapper.querySystem("USERNAME");
        String userName = systemParams.getValue();
        systemParams = systemParamMapper.querySystem("PASSWORD");
        String passWord = systemParams.getValue();
        if (StringUtils.isEmpty(userName) && StringUtils.isEmpty(passWord)){
            //未注册过
            return Result.error(ErrorCodeConstant.NO_REGISTER_ERROR,"账号尚未注册");
        }else {
            return Result.success();
        }
    }

    @Override
    public Result register(String username, String password) {
        SystemParam systemParams = systemParamMapper.querySystem("USERNAME");
        String userName = systemParams.getValue();
        systemParams = systemParamMapper.querySystem("PASSWORD");
        String passWord = systemParams.getValue();
        if (StringUtils.isEmpty(userName) && StringUtils.isEmpty(passWord)){
            systemParamUtil.updateSystemParam("USERNAME",null,username);
            systemParamUtil.updateSystemParam("PASSWORD",null,password);
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.REGISTER_ERROR,"已注册过，注册失败");
        }
    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    @Override
    public int addUser(User user){
        return userMapper.addUser(user);
    }

    /**
     * 查询用户
     * @param user
     * @return
     */
    @Override
    public User queryUser(User user){
        return userMapper.queryUser(user);
    }

    /**
     * 修改用户
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user){
        return userMapper.updateUser(user);
    }

    @Override
    public void saveUser(JSONObject content, String from_wxid, String ptPin, String wxpusherUid){
        User userQuery = new User();
        userQuery.setWxid(from_wxid);
        User user = userMapper.queryUser(userQuery);
        if (user == null) {
            user = new User();
            user.setWxid(from_wxid);
            user.setWxName(content.getString("from_name"));
            user.setJdPtPin(ptPin);
            user.setWxpusheruid(wxpusherUid);
            userMapper.addUser(user);
        } else {
            if (!user.getJdPtPin().contains(ptPin)) {
                user.setJdPtPin(user.getJdPtPin() + "#" + ptPin);
            }
            if (StringUtils.isNotEmpty(wxpusherUid) && wxpusherUid.equals(user.getWxpusheruid())) {
                user.setWxpusheruid(wxpusherUid);
            }
            user.setUpdatedTime(DateUtil.formatDateTime(new Date()));
            userMapper.updateUser(user);
        }
    }

    @Override
    public List<User> queryUsers(User userQuery) {
        return userMapper.queryUsers(userQuery);
    }
}
