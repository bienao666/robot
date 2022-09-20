package com.bienao.robot.controller.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.service.jingdong.CkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 京东ck
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/jdCk")
public class CkController {

    @Autowired
    private CkService ckService;

    /**
     * 添加ck
     * @param jdCkEntity
     * @return
     */
    @PostMapping("/addCk")
    public Result addCk(@RequestBody JdCkEntity jdCkEntity) {
        log.info("京东ck上传参数：{}", JSONObject.toJSONString(jdCkEntity));
        try {
            boolean flag = ckService.addCk(jdCkEntity);
            if (flag){
                return Result.success("添加成功");
            }else {
                return Result.error("-1","添加失败，请联系管理员");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("ck不存在或者ck已失效")){
                return Result.error("-1","添加失败，ck不存在或者ck已失效");
            }
            if (e.getMessage().equals("ck已经添加过")){
                return Result.error("-1","添加失败，ck已经添加过");
            }
            return Result.error("-1","添加失败，请联系管理员");
        }
    }

    /**
     * 检测ck是否过期
     * @return
     */
    @GetMapping("/checkCk")
    public void checkCk(){
        ckService.checkCk();
    }

    /**
     * 重置ck的助力数据
     */
    @GetMapping("/resetCkStatus")
    public void resetCkStatus(){
        ckService.resetCkStatus();
    }

    /**
     * 获取京东账号列表
     */
    @GetMapping("/getJdCks")
    public Result getJdCks(@RequestParam(value = "ck",required = false) String ck,
                           @RequestParam(value = "ptPin",required = false) String ptPin,
                           @RequestParam(value = "level",required = false) Integer level,
                           @RequestParam(value = "status",required = false) Integer status){
        List<JdCkEntity> jdcks = ckService.getJdCks(ck,ptPin,level,status);
        return Result.success(jdcks);
    }

    /**
     * 修改京东ck
     */
    @PostMapping("/updateJdCk")
    public Result updateJdCk(@RequestBody JdCkEntity jdCkEntity){
        if (jdCkEntity.getId()==null){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"id不能为空");
        }
        return ckService.updateJdCk(jdCkEntity);
    }

    /**
     * 删除京东ck
     */
    @PostMapping("/deleteJdCks")
    public Result deleteJdCks(@RequestBody List<Integer> ids){
        if (ids==null || ids.size()==0){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"ids不能为空");
        }
        return ckService.deleteJdCks(ids);
    }
}
