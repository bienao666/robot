package com.bienao.robot.controller.qinglong;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.ql.QlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result queryQls(@RequestParam(value = "id",required = false) String id){
        return qlService.queryQls(id);
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
     * 查询脚本
     * @return
     */
    @GetMapping("/queryScripts")
    public Result queryScripts(){
        return qlService.queryScripts();
    }
}
