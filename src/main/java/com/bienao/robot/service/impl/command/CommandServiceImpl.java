package com.bienao.robot.service.impl.command;

import com.bienao.robot.entity.CommandEntity;
import com.bienao.robot.mapper.CommandMapper;
import com.bienao.robot.service.command.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;

public class CommandServiceImpl implements CommandService {

    @Autowired
    private CommandMapper commandMapper;
    /**
     * 初始化命令
     */
    @Override
    @Async("asyncServiceExecutor")
    public void initializeCommand() {
        List<String> commands = commandMapper.queryAllCommand();
        ArrayList<CommandEntity> commandEntities = new ArrayList<>();
        addCommandEntities("我的uid","查询自己的uid","",commands,commandEntities);
        addCommandEntities("myuid","查询自己的uid","",commands,commandEntities);
        addCommandEntities("监听","监听此群消息","开始监听",commands,commandEntities);
        addCommandEntities("取消监听","取消监听此群消息","取消监听成功",commands,commandEntities);
        commandMapper.addCommands(commandEntities);
    }

    private void addCommandEntities(String command,String function,String reply,List<String> commands,ArrayList<CommandEntity> commandEntities){
        if (!commands.contains(commands)){
            CommandEntity commandEntity = new CommandEntity();
            commandEntity.setCommand(command);
            commandEntity.setFunction(function);
            commandEntity.setReply(reply);
            commandEntities.add(commandEntity);
        }
    }
}
