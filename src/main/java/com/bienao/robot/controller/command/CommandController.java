package com.bienao.robot.controller.command;

import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.CommandEntity;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.command.CommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 命令
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/command")
public class CommandController {

    @Autowired
    private CommandService commandService;

    /**
     * 添加命令
     * @param commandEntity
     * @return
     */
    @LoginToken
    @PostMapping("/addCommand")
    public Result addCommand(@RequestBody CommandEntity commandEntity){
        return commandService.addCommand(commandEntity);
    }

    /**
     * 查询命令
     * @param command
     * @return
     */
    @LoginToken
    @GetMapping("/queryCommand")
    public Result queryCommand(@RequestParam(value = "command",required = false) String command,
                               @RequestParam(value = "function",required = false) String function){
        return commandService.queryCommand(command,function);
    }

    /**
     * 修改命令
     * @param commandEntity
     * @return
     */
    @LoginToken
    @PostMapping("/updateCommand")
    public Result updateCommand(@RequestBody CommandEntity commandEntity){
        return commandService.updateCommand(commandEntity);
    }

    /**
     * 删除命令
     * @param ids
     * @return
     */
    @LoginToken
    @PostMapping("/deleteCommand")
    public Result deleteCommand(@RequestBody List<Integer> ids){
        return commandService.deleteCommand(ids);
    }
}
