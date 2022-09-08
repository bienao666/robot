package com.bienao.robot.controller.qinglong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.WireKeyEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.ql.WireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线报接口
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/ql/wire")
public class WireController {

    @Autowired
    private WireService wireService;

    /**
     * 添加线报活动
     * @param wireEntity
     */
    @PostMapping("/addWire")
    public Result addWire(@Validated @RequestBody WireEntity wireEntity){
        long start = System.currentTimeMillis();
        log.info("开始addWire：{}",System.currentTimeMillis());
        Result result = null;
        try {
            List<WireKeyEntity> keys = wireEntity.getKeys();
            if (keys == null || keys.size() ==0){
                return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"洞察变量为空");
            }
            result = wireService.addWire(wireEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"添加失败");
        }
        long end = System.currentTimeMillis();
        log.info("addWire结束：{}",System.currentTimeMillis());
        log.info("addWire耗时：{}ms",(end-start));
        return result;
    }

    /**
     * 查询线报活动
     * @param key
     */
    @GetMapping("/queryWire")
    public Result queryWire(@RequestParam(value = "key",required = false) String key){
        long start = System.currentTimeMillis();
        log.info("开始queryWire：{}",System.currentTimeMillis());
        Result result = wireService.queryWire(key);
        long end = System.currentTimeMillis();
        log.info("queryWire结束：{}",System.currentTimeMillis());
        log.info("queryWire耗时：{}ms",(end-start));
        return result;
    }

    /**
     * 修改线报活动
     * @param wireEntity
     */
    @PostMapping("/updateWire")
    public Result updateWire(@RequestBody WireEntity wireEntity){
        long start = System.currentTimeMillis();
        log.info("开始updateWire：{}",System.currentTimeMillis());
        Result result;
        try {
            Integer id = wireEntity.getId();
            if (id==null){
                return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"参数异常");
            }
            result = wireService.updateWire(wireEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"修改失败");
        }
        long end = System.currentTimeMillis();
        log.info("updateWire结束：{}",System.currentTimeMillis());
        log.info("updateWire耗时：{}ms",(end-start));
        return result;
    }

    /**
     * 删除线报活动
     * @param wireIds
     */
        @PostMapping("/deleteWire")
    public Result deleteWire(@RequestBody List<Integer> wireIds){
        long start = System.currentTimeMillis();
        log.info("开始deleteWire：{}",System.currentTimeMillis());
        Result result = null;
        try {
            result = wireService.deleteWire(wireIds);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"删除失败");
        }
        long end = System.currentTimeMillis();
        log.info("deleteWire结束：{}",System.currentTimeMillis());
        log.info("deleteWire耗时：{}ms",(end-start));
        return result;
    }

    /**
     * 添加洞察变量
     * @param jsonObject
     */
    @PostMapping("/addKey")
    public Result addKey(@Validated @RequestBody JSONObject jsonObject){
        long start = System.currentTimeMillis();
        log.info("开始addWire：{}",System.currentTimeMillis());
        Result result = null;
        try {
            Integer id = jsonObject.getInteger("id");
            if (id == null){
                return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"id不能为空");
            }
            result = wireService.addKey(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"添加失败");
        }
        long end = System.currentTimeMillis();
        log.info("addWire结束：{}",System.currentTimeMillis());
        log.info("addWire耗时：{}ms",(end-start));
        return result;
    }
}
