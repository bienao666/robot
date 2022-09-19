package com.bienao.robot.service.ql;

import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.Result;

import java.util.List;

public interface WireService {

    /**
     * 添加活动
     * @param wireEntity
     * @return
     */
    Result addWire(WireEntity wireEntity);

    /**
     * 查询活动
     * @param key
     * @return
     */
    Result queryWire(String key,Integer pageNo,Integer pageSize);

    /**
     * 添加活动
     * @param activityName
     * @param script
     * @param keys
     */
    void addWire(String activityName, String script, List<String> keys);

    /**
     * 修改活动
     * @param wireEntity
     */
    Result updateWire(WireEntity wireEntity);

    /**
     * 删除活动
     * @param wireIds
     * @return
     */
    Result deleteWire(List<Integer> wireIds);

    /**
     * 执行线报活动
     * @param wire
     * @return
     */
    Result handleActivity(Integer id,String script,String wire);

    /**
     * 添加线报活动
     * @param wire
     * @return
     */
    Result addActivity(String wire);

    /**
     * 查询线报活动
     * @return
     */
    Result queryActivity(Integer pageNo,Integer pageSize);

    void handleWire(Integer id,String script,String content);
}
