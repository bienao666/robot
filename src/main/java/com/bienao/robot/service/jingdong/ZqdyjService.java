package com.bienao.robot.service.jingdong;

import com.bienao.robot.entity.Result;

public interface ZqdyjService {

    /**
     * 赚钱大赢家助力
     * @param param
     * @return
     */
    Result help(String param);

    void reset();

    Result test(String account);

    Result getZqdyjData();
}
