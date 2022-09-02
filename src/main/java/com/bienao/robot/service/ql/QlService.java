package com.bienao.robot.service.ql;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.result.Result;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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
    Result addQl(QlEntity ql);

    /**
     * 查询所有青龙
     * @return
     */
    Result queryQls(List<Integer> ids);

    /**
     * 更新青龙
     * @param ql
     */
    Result updateQl(QlEntity ql);

    /**
     * 查询脚本
     * @return
     */
    Result queryScripts(String key);

    /**
     * 执行脚本
     * @param command
     * @return
     */
    Result runScript(String command, List<Integer> ids);

    /**
     * 删除青龙
     * @param ids
     * @return
     */
    Result deleteQls(List<Integer> ids);
}
