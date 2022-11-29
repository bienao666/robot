package com.bienao.robot.service.weixin;

import com.bienao.robot.entity.ForwardEntity;
import com.bienao.robot.entity.Group;

import java.util.List;

public interface ForwardService {
    /**
     * 添加转发
     * @param
     * @return
     */
    int addForward(String from,String fromName,Integer fromtype ,String to,String toName,Integer totype);

    int addForward(Group fromGroup, Group toGroup);

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
    int deleteForward(List<Integer> ids);
}
