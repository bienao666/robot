package com.bienao.robot.controller.qinglong;

import com.alibaba.fastjson.JSONObject;
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
     * @param jsonObject
     */
    @PostMapping("/addQl")
    public Result addQl(@RequestBody JSONObject jsonObject){
        return qlService.addQl(jsonObject);
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
    @GetMapping("/updateQl")
    public Result updateQl(@RequestBody JSONObject jsonObject){
        return qlService.updateQl(jsonObject);
    }
}
