package com.bienao.robot.service.impl.ql;

import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.WireKeyEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.WireKeyMapper;
import com.bienao.robot.mapper.WireMapper;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.ql.WireKeyService;
import com.bienao.robot.service.ql.WireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class WireServiceImpl implements WireService {

    @Autowired
    private WireMapper wireMapper;

    @Autowired
    private WireKeyMapper wireKeyMapper;

    /**
     * 添加线报活动
     * @param wireEntity
     * @return
     */
    @Override
    @Transactional
    public Result addWire(WireEntity wireEntity) {
        int i = wireMapper.addWire(wireEntity);
        if (i==0) {
            throw new RuntimeException("数据库操作异常");
        }
        Integer maxId = wireMapper.queryMaxId();
        List<WireKeyEntity> keys = wireEntity.getKeys();
        for (WireKeyEntity key : keys) {
            key.setWireid(maxId);
        }
        i = wireKeyMapper.addWireKey(keys);
        if (i==0){
            throw new RuntimeException("数据库操作异常");
        }
        return Result.success("添加成功");
    }

    /**
     * 添加线报活动
     * @param
     * @return
     */
    @Override
    public void addWire(String activityName,String script,List<String> keys){
        WireEntity wireEntity = new WireEntity(activityName,script);
        Integer res = wireMapper.addWire(wireEntity);
        if (res!=0){
            Integer maxId = wireMapper.queryMaxId();
            ArrayList<WireKeyEntity> wireKeyEntities = new ArrayList<>();
            for (String key : keys) {
                WireKeyEntity wireKeyEntity = new WireKeyEntity(maxId,key);
                wireKeyEntities.add(wireKeyEntity);
            }
            wireKeyMapper.addWireKey(wireKeyEntities);
        }
        keys.clear();
    }

    /**
     * 修改线报活动
     * @param wireEntity
     */
    @Override
    public Result updateWire(WireEntity wireEntity) {
        wireEntity.setUpdatedTime(new Date());
        wireMapper.updateWire(wireEntity);
        for (WireKeyEntity key : wireEntity.getKeys()) {
            key.setUpdatedTime(new Date());
            wireKeyMapper.updateWireKey(key);
        }
        return Result.success("修改成功");
    }

    /**
     * 删除线报活动
     * @param ids
     * @return
     */
    @Override
    public Result deleteWire(List<Integer> ids) {
        Integer res = wireMapper.deleteWire(ids);
        if (res==0){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"删除失败");
        }else {

        }
        return null;
    }

    /**
     * 查询线报活动
     * @param key
     * @return
     */
    @Override
    public Result queryWire(String key) {
        List<WireEntity> wireEntities = wireMapper.queryWire(key);
        return Result.success(wireEntities);
    }


}
