package com.bienao.robot.controller.qinglong;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.LoginToken;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.entity.Result;
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
     *
     * @param ql
     */
    @LoginToken
    @PostMapping("/addQl")
    public Result addQl(@RequestBody QlEntity ql) {
        long start = System.currentTimeMillis();
        log.info("开始addQl：{}", System.currentTimeMillis());
        Result result = qlService.addQl(ql);
        long end = System.currentTimeMillis();
        log.info("addQl结束：{}", System.currentTimeMillis());
        log.info("addQl耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 查询青龙
     *
     * @return
     */
    @LoginToken
    @GetMapping("/queryQls")
    public Result queryQls(@RequestParam(value = "ids", required = false) List<Integer> ids) {
        long start = System.currentTimeMillis();
        log.info("开始queryQls：{}", System.currentTimeMillis());
        Result result = qlService.queryQls(ids);
        long end = System.currentTimeMillis();
        log.info("queryQls结束：{}", System.currentTimeMillis());
        log.info("queryQls耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 更新青龙
     *
     * @return
     */
    @LoginToken
    @PostMapping("/updateQl")
    public Result updateQl(@RequestBody QlEntity ql) {
        long start = System.currentTimeMillis();
        log.info("开始updateQl：{}", System.currentTimeMillis());
        Result result = qlService.updateQl(ql);
        long end = System.currentTimeMillis();
        log.info("updateQl结束：{}", System.currentTimeMillis());
        log.info("updateQl耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 删除青龙
     *
     * @return
     */
    @LoginToken
    @GetMapping("/deleteQls")
    public Result deleteQls(@RequestParam(value = "ids") List<Integer> ids) {
        if (ids.size() == 0) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        long start = System.currentTimeMillis();
        log.info("开始deleteQls：{}", System.currentTimeMillis());
        Result result = qlService.deleteQls(ids);
        long end = System.currentTimeMillis();
        log.info("deleteQls结束：{}", System.currentTimeMillis());
        log.info("deleteQls耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 查询脚本
     *
     * @return
     */
    @LoginToken
    @GetMapping("/queryScripts")
    public Result queryScripts(@RequestParam(value = "key", required = false) String key,
                               @RequestParam(value = "pageNo") Integer pageNo,
                               @RequestParam(value = "pageSize") Integer pageSize) {
        long start = System.currentTimeMillis();
        log.info("开始queryScripts：{}", System.currentTimeMillis());
        Result result = qlService.queryScripts(key, pageNo, pageSize);
        long end = System.currentTimeMillis();
        log.info("queryScripts结束：{}", System.currentTimeMillis());
        log.info("queryScripts耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 执行脚本
     *
     * @return
     */
    @LoginToken
    @PostMapping("/runScript")
    public Result runScript(@RequestBody JSONObject jsonObject) {
        //任务
        String command = jsonObject.getString("command");
        if (StringUtils.isEmpty(command)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "command不能为空");
        }
        String idsSr = jsonObject.getString("ids");
        if (StringUtils.isEmpty(idsSr)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        List<Integer> ids = JSON.parseArray(idsSr, Integer.class);
        if (ids.size() == 0) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        long start = System.currentTimeMillis();
        log.info("开始runScript：{}", System.currentTimeMillis());
        Result result = qlService.runScript(command, ids);
        long end = System.currentTimeMillis();
        log.info("runScript结束：{}", System.currentTimeMillis());
        log.info("runScript耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 停止脚本
     *
     * @return
     */
    @LoginToken
    @PostMapping("/stopScript")
    public Result stopScript(@RequestBody JSONObject jsonObject) {
        //任务
        String command = jsonObject.getString("command");
        if (StringUtils.isEmpty(command)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "command不能为空");
        }
        String idsSr = jsonObject.getString("ids");
        if (StringUtils.isEmpty(idsSr)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        List<Integer> ids = JSON.parseArray(idsSr, Integer.class);
        if (ids.size() == 0) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        long start = System.currentTimeMillis();
        log.info("开始stopScript：{}", System.currentTimeMillis());
        Result result = qlService.stopScript(command, ids);
        long end = System.currentTimeMillis();
        log.info("stopScript结束：{}", System.currentTimeMillis());
        log.info("stopScript耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 置顶脚本
     *
     * @return
     */
    @LoginToken
    @PostMapping("/pinScript")
    public Result pinScript(@RequestBody JSONObject jsonObject) {
        //任务
        String command = jsonObject.getString("command");
        if (StringUtils.isEmpty(command)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "command不能为空");
        }
        String idsSr = jsonObject.getString("ids");
        if (StringUtils.isEmpty(idsSr)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        List<Integer> ids = JSON.parseArray(idsSr, Integer.class);
        if (ids.size() == 0) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        long start = System.currentTimeMillis();
        log.info("开始pinScript：{}", System.currentTimeMillis());
        Result result = qlService.pinScript(command, ids);
        long end = System.currentTimeMillis();
        log.info("pinScript结束：{}", System.currentTimeMillis());
        log.info("pinScript耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 禁用脚本
     *
     * @return
     */
    @LoginToken
    @PostMapping("/disableScript")
    public Result disableScript(@RequestBody JSONObject jsonObject) {
        //任务
        String command = jsonObject.getString("command");
        if (StringUtils.isEmpty(command)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "command不能为空");
        }
        String idsSr = jsonObject.getString("ids");
        if (StringUtils.isEmpty(idsSr)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        List<Integer> ids = JSON.parseArray(idsSr, Integer.class);
        if (ids.size() == 0) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        long start = System.currentTimeMillis();
        log.info("开始disableScript：{}", System.currentTimeMillis());
        Result result = qlService.disableScript(command, ids);
        long end = System.currentTimeMillis();
        log.info("disableScript结束：{}", System.currentTimeMillis());
        log.info("disableScript耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 启用脚本
     *
     * @return
     */
    @LoginToken
    @PostMapping("/enableScript")
    public Result enableScript(@RequestBody JSONObject jsonObject) {
        //任务
        String command = jsonObject.getString("command");
        if (StringUtils.isEmpty(command)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "command不能为空");
        }
        String idsSr = jsonObject.getString("ids");
        if (StringUtils.isEmpty(idsSr)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        List<Integer> ids = JSON.parseArray(idsSr, Integer.class);
        if (ids.size() == 0) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "ids不能为空");
        }
        long start = System.currentTimeMillis();
        log.info("开始enableScript：{}", System.currentTimeMillis());
        Result result = qlService.enableScript(command, ids);
        long end = System.currentTimeMillis();
        log.info("enableScript结束：{}", System.currentTimeMillis());
        log.info("enableScript耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 一键车头
     *
     * @return
     */
    @LoginToken
    @GetMapping("/oneKeyHead")
    public Result oneKeyHead() {
        long start = System.currentTimeMillis();
        log.info("开始oneKeyHead：{}", System.currentTimeMillis());
        Result result = qlService.oneKeyHead();
        long end = System.currentTimeMillis();
        log.info("oneKeyHead结束：{}", System.currentTimeMillis());
        log.info("oneKeyHead耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 取消车头
     *
     * @return
     */
    @LoginToken
    @GetMapping("/cancelHead")
    public Result cancelHead() {
        long start = System.currentTimeMillis();
        log.info("开始cancelHead：{}", System.currentTimeMillis());
        Result result = qlService.cancelHead();
        long end = System.currentTimeMillis();
        log.info("cancelHead结束：{}", System.currentTimeMillis());
        log.info("cancelHead耗时：{}ms", (end - start));
        return result;
    }

    /**
     * 韭菜友好
     *
     * @return
     */
    @LoginToken
    @GetMapping("/leekFriendly")
    public Result leekFriendly() {
        qlService.leekFriendly();
        return Result.success();
    }

    /**
     * 多青龙 ck分布优化
     */
    @LoginToken
    @GetMapping("/autoAdjust")
    public Result autoAdjust() {
        qlService.autoAdjust();
        return Result.success();
    }

    /**
     * 释放青龙ck
     */
    @LoginToken
    @GetMapping("/releaseCk")
    public void releaseCk() {

    }

    /**
     * 获取待恢复青龙
     */
    @LoginToken
    @GetMapping("/getRecoveryQl")
    public Result getRecoveryQl(){
        Result result = qlService.getRecoveryQl();
        return result;
    }

    /**
     * 恢复青龙ck
     */
    @LoginToken
    @PostMapping("/recoveryCk")
    public Result recoveryCk(@RequestBody JSONObject jsonObject) {
        //老青龙备注
        String oldQl = jsonObject.getString("oldQl");
        if (StringUtils.isEmpty(oldQl)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "老青龙不能为空");
        }
        //新青龙备注
        String newQl = jsonObject.getString("newQl");
        if (StringUtils.isEmpty(newQl)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "新青龙不能为空");
        }
        Result result = qlService.recoveryCk(oldQl,newQl);
        return result;
    }

    /**
     * 京东红包领取通知
     */
    @LoginToken
    @GetMapping("/notifyRedPacket")
    public void notifyRedPacket(){
        qlService.notifyRedPacket();
    }

}
