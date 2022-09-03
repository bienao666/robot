package com.bienao.robot.controller.systemParam;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.result.Result;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统参数接口
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/sysParam")
public class SystemParamController {

    @Autowired
    private SystemParamUtil systemParamUtil;

    /**
     * 添加参数
     * @param systemParam
     */
    @PostMapping("/addSystemParam")
    public Result addSystemParam(@RequestBody SystemParam systemParam){
        boolean flag = systemParamUtil.addSystemParam(systemParam);
        if (flag){
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR,"添加失败");
        }
    }

    /**
     * 修改参数
     * @param systemParam
     */
    @PostMapping("/updateSystemParam")
    public Result updateSystemParam(@RequestBody SystemParam systemParam){
        String code = systemParam.getCode();
        if (StringUtils.isEmpty(code)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"code参数不能为空");
        }
        String codeName = systemParam.getCodeName();
        String value = systemParam.getValue();
        boolean flag = systemParamUtil.updateSystemParam(code,codeName,value);
        if (flag){
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR,"添加失败");
        }
    }

    /**
     * 查询参数
     * @param code
     */
    @GetMapping("/querySystemParams")
    public Result querySystemParams(@RequestParam(value = "code",required = false) String code){
        List<SystemParam> systemParams = systemParamUtil.querySystemParams(code);
        return Result.success(systemParams);
    }

    /**
     * 删除参数
     * @return
     */
    @PostMapping("/deleteSystemParams")
    public Result deleteSystemParams(@RequestBody List<Integer> ids){
        if(ids.size()==0){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"ids不能为空");
        }
        return systemParamUtil.deleteSystemParams(ids);
    }
}
