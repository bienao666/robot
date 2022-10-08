package com.bienao.robot.service.weixin;

import com.bienao.robot.entity.ForwardEntity;

import java.util.List;

public interface ForwardService {
    /**
     * 添加转发
     * @param
     * @return
     */
    int addForward(String from,String fromName,Integer fromtype ,String to,String toName,Integer totype);

    /**
     * 查询转发
     *
     * @return
     */
    List<ForwardEntity> queryForward(String from, String fromName, String to, String toName);

    /**
     * 删除
     * @param ids
     */
    void deleteForward(List<Integer> ids);
}
