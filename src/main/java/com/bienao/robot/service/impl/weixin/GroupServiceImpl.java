package com.bienao.robot.service.impl.weixin;

import com.bienao.robot.entity.Group;
import com.bienao.robot.mapper.GroupMapper;
import com.bienao.robot.service.weixin.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public int addGroup(Group group) {
        return groupMapper.addGroup(group);
    }

    @Override
    public List<Group> queryGroupByFunctionType(Integer functionType) {
        return groupMapper.queryGroupByFunctionType(functionType);
    }

    @Override
    public Group queryGroupByGroupIdAndFunctionType(String groupid, Integer functionType) {
        return groupMapper.queryGroupByGroupIdAndFunctionType(groupid,functionType);
    }

    @Override
    public int deleteGroupByGroupIdAndFunctionType(String groupid, Integer functionType) {
        return groupMapper.deleteGroupByGroupIdAndFunctionType(groupid,functionType);
    }

    @Override
    public List<Group> queryGroupByGroupId(String groupid) {
        return groupMapper.queryGroupByGroupId(groupid);
    }
}
