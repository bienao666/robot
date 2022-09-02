package com.bienao.robot.controller.qinglong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.ql.QlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 青龙接口
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/ql")
public class QlController {

    @Autowired
    private QlService qlService;

    /**
     * 添加青龙
     * @param ql
     */
    @PostMapping("/addQl")
    public Result addQl(@RequestBody QlEntity ql){
        return qlService.addQl(ql);
    }

    /**
     * 查询青龙
     * @return
     */
    @GetMapping("/queryQls")
    public Result queryQls(@RequestParam(value = "ids",required = false) List<Integer> ids){
        return qlService.queryQls(ids);
    }

    /**
     * 更新青龙
     * @return
     */
    @PostMapping("/updateQl")
    public Result updateQl(@RequestBody QlEntity ql){
        return qlService.updateQl(ql);
    }

    /**
     * 删除青龙
     * @return
     */
    @GetMapping("/deleteQls")
    public Result deleteQls(@RequestParam(value = "ids",required = false) List<Integer> ids){
        return qlService.deleteQls(ids);
    }

    /**
     * 查询脚本
     * @return
     */
    @GetMapping("/queryScripts")
    public Result queryScripts(@RequestParam(value = "key",required = false) String key){
        return qlService.queryScripts(key);
    }

    /**
     * 执行脚本
     * @return
     */
    @PostMapping("/runScript")
    public Result runScript(@RequestBody JSONObject jsonObject){
        //任务
        String command = jsonObject.getString("command");
        if (StringUtils.isEmpty(command)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"command不能为空");
        }
        String idsSr = jsonObject.getString("ids");
        if (StringUtils.isEmpty(idsSr)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"ids不能为空");
        }
        List<Integer> ids = JSON.parseArray(idsSr, Integer.class);
        if (ids.size()==0){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"ids不能为空");
        }
        return qlService.runScript(command,ids);
    }

    /**
     * 一键车头
     * @return
     */
    @PostMapping("/oneKeyHead")
    public Result oneKeyHead(){
        return Result.success();
    }
}
