package com.bienao.robot.service.impl.weixin;

import com.bienao.robot.Constants.MessageFromType;
import com.bienao.robot.entity.ForwardEntity;
import com.bienao.robot.entity.Group;
import com.bienao.robot.mapper.ForwardMapper;
import com.bienao.robot.service.weixin.ForwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ForwardServiceImpl implements ForwardService {

    @Autowired
    private ForwardMapper forwardMapper;

    @Override
    public int addForward(String from, String fromName, Integer fromtype, String to, String toName, Integer totype) {
        return forwardMapper.addForward(from, fromName, fromtype, to, toName, totype);
    }

    @Override
    public int addForward(Group fromGroup,Group toGroup) {
        String fromGroupid = fromGroup.getGroupid();
        String toGroupid = toGroup.getGroupid();

        return forwardMapper.addForward(fromGroupid, fromGroup.getGroupName(), getTypeByGroupId(fromGroupid), toGroupid, toGroup.getGroupName(), getTypeByGroupId(toGroupid));
    }

    private int getTypeByGroupId(String groupid){
        if (groupid.startsWith("-")){
            return MessageFromType.tgq;
        }
        return MessageFromType.wxq;
    }

    @Override
    public List<ForwardEntity> queryForward(String from, String fromName, String to, String toName) {
        return forwardMapper.queryForward(from, fromName, to, toName);
    }

    @Override
    public int deleteForward(List<Integer> ids) {
        return forwardMapper.deleteForward(ids);
    }
}
