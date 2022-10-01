package com.bienao.robot.service.ql;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.Result;

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
    Result queryScripts(String key,Integer pageNo,Integer pageSize);

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

    /**
     * 一键车头
     * @return
     */
    Result oneKeyHead();

    /**
     * 取消车头
     * @return
     */
    Result cancelHead();

    /**
     * 停止脚本
     * @param command
     * @return
     */
    Result stopScript(String command, List<Integer> ids);

    /**
     * 置顶脚本
     * @param command
     * @param ids
     * @return
     */
    Result pinScript(String command, List<Integer> ids);

    /**
     * 禁用脚本
     * @param command
     * @param ids
     * @return
     */
    Result disableScript(String command, List<Integer> ids);

    /**
     * 启用脚本
     * @param command
     * @param ids
     * @return
     */
    Result enableScript(String command, List<Integer> ids);

    /**
     * 添加京东ck
     * @return
     */
    void addJdCk(JSONObject content,String ck, String ptPin, String wxPusherUid);

    void setSmallHead();

    /**
     * 韭菜友好设置
     */
    void leekFriendly();
}
