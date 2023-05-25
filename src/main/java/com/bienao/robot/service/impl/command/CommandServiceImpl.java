package com.bienao.robot.service.impl.command;

import com.bienao.robot.entity.CommandEntity;
import com.bienao.robot.entity.Result;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.CommandMapper;
import com.bienao.robot.service.command.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommandServiceImpl implements CommandService {

    @Autowired
    private CommandMapper commandMapper;

    @Override
    public Result addCommand(CommandEntity commandEntity) {
        commandEntity.setIsBuiltIn(0);
        int i = commandMapper.addCommand(commandEntity);
        if (i==1){
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"添加失败");
        }
    }

    @Override
    public List<CommandEntity> queryCommand(String command, String function) {
        List<CommandEntity> commandEntities = commandMapper.queryCommand(command, function,null);
        return commandEntities;
    }

    @Override
    public Result updateCommand(CommandEntity commandEntity) {
        int i = commandMapper.updateCommand(commandEntity);
        if (i==1){
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"修改失败");
        }
    }

    @Override
    public Result deleteCommand(List<Integer> ids) {
        int i = commandMapper.deleteCommand(ids);
        if (i==0){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"删除失败");
        }else {
            return Result.success();
        }
    }


}
