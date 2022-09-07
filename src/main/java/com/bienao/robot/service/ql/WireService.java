package com.bienao.robot.service.ql;

import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.result.Result;

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
    Result queryWire(String key);

    /**
     * 添加活动
     * @param activityName
     * @param script
     * @param keys
     */
    void addWire(String activityName, String script, List<String> keys);

    /**
     * 修改线报活动
     * @param wireEntity
     */
    Result updateWire(WireEntity wireEntity);

    /**
     * 删除线报活动
     * @param wireIds
     * @return
     */
    Result deleteWire(List<Integer> wireIds);
}
