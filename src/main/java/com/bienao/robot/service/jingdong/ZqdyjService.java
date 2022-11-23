package com.bienao.robot.service.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdZqdyjEntity;

import java.util.List;

public interface ZqdyjService {

    /**
     * 赚钱大赢家助力
     * @param param
     * @return
     */
    Result helpZqdyj(String param,boolean isTime,String remark);

    void reset();

    Result test(String account);

    Result getZqdyjData();

    Result resetHot();

    List<JdZqdyjEntity> getHelpList();

    Result delete(Integer id);

    List<JSONObject> getZqdyjCk();

    void help(Integer zqdyjId,String needHelpPtPin,String sId,String needHelpck,String remark,List<JSONObject> zqdyjCk);


    /**
     * 赚钱大赢家助力-助力列表助力
     */
    Result help2(Integer id, String helpCode, String ck,String remark);
}
