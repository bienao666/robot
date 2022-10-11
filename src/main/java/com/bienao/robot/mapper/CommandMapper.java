package com.bienao.robot.mapper;

import com.bienao.robot.entity.CommandEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommandMapper {

    /**
     * 添加命令
     * @param commandEntity
     * @return
     */
    int addCommand(CommandEntity commandEntity);

    /**
     * 批量添加命令
     * @param commandEntitList
     * @return
     */
    int addCommands(List<CommandEntity> commandEntitList);

    /**
     * 查询命令
     * @param command
     * @return
     */
    List<CommandEntity> queryCommand(String command,String function,Integer isBuiltIn);

    /**
     * 查询命令
     * @return
     */
    List<String> queryAllCommand();

    /**
     * 修改命令
     * @param commandEntity
     * @return
     */
    Integer updateCommand(CommandEntity commandEntity);

    /**
     * 删除命令
     * @param ids
     * @return
     */
    Integer deleteCommand(List<Integer> ids);
}
