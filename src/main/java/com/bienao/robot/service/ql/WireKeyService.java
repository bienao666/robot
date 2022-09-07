package com.bienao.robot.service.ql;

import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.WireKeyEntity;
import com.bienao.robot.result.Result;

import java.util.List;

public interface WireKeyService {

    /**
     * 添加活动洞察变量
     * @param wireId
     * @param wireKeyEntities
     * @return
     */
    Result addWireKey(Integer wireId, List<WireKeyEntity> wireKeyEntities);
}
