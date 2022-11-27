package com.bienao.robot.service.dianxin;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Result;

public interface DianXinService {

    Result lotteryLive(String phone , String passWord);
}
