package com.bienao.robot.service.command;

import com.bienao.robot.entity.CommandEntity;
import com.bienao.robot.entity.Result;

import java.util.List;

public interface CommandService {

    /**
     * 初始化命令
     */
    void initializeCommand();

    /**
     * 添加命令
     * @param commandEntity
     * @return
     */
    Result addCommand(CommandEntity commandEntity);

    /**
     * 查询命令
     * @param command
     * @return
     */
    Result queryCommand(String command, String function);

    /**
     * 修改命令
     * @param commandEntity
     * @return
     */
    Result updateCommand(CommandEntity commandEntity);

    /**
     * 删除命令
     * @param ids
     * @return
     */
    Result deleteCommand(List<Integer> ids);
}
