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
    /**
     * 初始化命令
     */
    @Override
    @Async("asyncServiceExecutor")
    public void initializeCommand() {
        List<String> commands = commandMapper.queryAllCommand();
        ArrayList<CommandEntity> commandEntities = new ArrayList<>();
        addCommandEntities("我的uid","查询自己的uid","",1,commands,commandEntities);
        addCommandEntities("myuid","查询自己的uid","",1,commands,commandEntities);
        addCommandEntities("群号","查询微信群群号","",1,commands,commandEntities);
        addCommandEntities("群号 xxx","根据微信群名称查询微信群群号","",1,commands,commandEntities);
        addCommandEntities("群号列表","查询所有群号","",1,commands,commandEntities);
        addCommandEntities("监听","监听此群消息","开始监听",1,commands,commandEntities);
        addCommandEntities("取消监听","取消监听此群消息","取消监听成功",1,commands,commandEntities);
        addCommandEntities("q","退出当前操作","已退出",1,commands,commandEntities);
        addCommandEntities("博客","查询博客地址","",1,commands,commandEntities);
        addCommandEntities("登陆","京东短信登录","",1,commands,commandEntities);
        addCommandEntities("饿了么","获取饿了么赚现金推广图片","",1,commands,commandEntities);
        addCommandEntities("支付宝红包","获取支付宝红包商家二维码图片","",1,commands,commandEntities);
        addCommandEntities("微博","当前微博热搜前20条","",1,commands,commandEntities);
        addCommandEntities("监控茅台洋河","监控茅台洋河活动","监控成功",1,commands,commandEntities);
        addCommandEntities("取消监控茅台洋河","取消监控茅台洋河活动","取消茅台洋河监控成功",1,commands,commandEntities);
        addCommandEntities("推送饿了么","定时推送饿了么赚现金推广图片","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送饿了么","取消定时推送饿了么赚现金推广图片","取消饿了么推送成功",1,commands,commandEntities);
        addCommandEntities("推送摸鱼","定时推送摸鱼信息","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送摸鱼","取消定时推送摸鱼信息","取消饿了么推送成功",1,commands,commandEntities);
        addCommandEntities("推送微博","定时推送当前微博热搜前20条","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送微博","取消定时推送当前微博热搜前20条","取消推送微博成功",1,commands,commandEntities);
        addCommandEntities("推送支付宝红包","定时推送支付宝红包","开启推送成功",1,commands,commandEntities);
        addCommandEntities("取消推送支付宝红包","取消定时推送支付宝红包","取消推送支付宝红包成功",1,commands,commandEntities);
        addCommandEntities("举牌 xxx","根据xxx的内容生成一张图片","",1,commands,commandEntities);
        addCommandEntities("xxx油价","查询xxx省的油价","",1,commands,commandEntities);
        addCommandEntities("买家秀","随机获取一张买家秀图片","",1,commands,commandEntities);
        addCommandEntities("老色批","随机获取一张老色批图片","",1,commands,commandEntities);
        addCommandEntities("xx天气","查询xx城市的天气","",1,commands,commandEntities);
        addCommandEntities("比价","根据商品链接查询价格信息","",1,commands,commandEntities);
        addCommandEntities("启用 xxx","启用京东ck xxx","启用成功",1,commands,commandEntities);
        addCommandEntities("禁用 xxx","禁用京东ck xxx","禁用成功",1,commands,commandEntities);
        addCommandEntities("转发 群号1 群号2","转发群号1的消息到群号2","",1,commands,commandEntities);
        addCommandEntities("取消转发 群号1 群号2","取消转发群号1的消到群号2","",1,commands,commandEntities);
        addCommandEntities("扭","随机获取一个小姐姐跳舞短视频","",1,commands,commandEntities);
        if (commandEntities.size()>0){
            commandMapper.addCommands(commandEntities);
        }
    }

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

    private void addCommandEntities(String command,String function,String reply,Integer isBuiltIn,List<String> commands,ArrayList<CommandEntity> commandEntities){
        if (!commands.contains(command)){
            CommandEntity commandEntity = new CommandEntity();
            commandEntity.setCommand(command);
            commandEntity.setFunction(function);
            commandEntity.setReply(reply);
            commandEntity.setIsBuiltIn(isBuiltIn);
            commandEntities.add(commandEntity);
        }
    }
}
