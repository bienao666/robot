package com.bienao.robot.service.ql;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.result.Result;
import org.springframework.web.bind.annotation.RequestBody;

public interface QlService {

    /**
     * 一键设置大车头
     * @return
     */
    public boolean oneKeyBigHead(JSONObject content);

    /**
     * 添加青龙
     * @param ql
     */
    Result addQl(JSONObject ql);

    /**
     * 查询所有青龙
     * @return
     */
    Result queryQls();

    /**
     * 查询青龙
     * @param url
     * @return
     */
    Result queryQl(String url);

    /**
     * 更新青龙
     * @param ql
     */
    Result updateQl(JSONObject ql);
}
