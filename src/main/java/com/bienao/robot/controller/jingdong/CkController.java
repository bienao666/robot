package com.bienao.robot.controller.jingdong;

import com.bienao.robot.result.Result;
import com.bienao.robot.service.jingdong.CkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 京东ck上传
     * @param ck
     * @return
     */
    @GetMapping("/addCk")
    public Result addCk(@RequestParam String ck) {
        log.info("京东ck上传参数：{}", ck);
        if (StringUtils.isEmpty(ck)){
            return Result.error("-1","ck不能为空");
        }

        try {
            boolean flag = ckService.addCk(ck,3);
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
     * 京东工具人ck上传
     * @param ck
     * @return
     */
    @GetMapping("/addGjCk")
    public Result addGjCk(@RequestParam String ck) {
        log.info("京东工具人ck上传参数：{}", ck);
        if (StringUtils.isEmpty(ck)){
            return Result.error("-1","ck不能为空");
        }

        try {
            boolean flag = ckService.addCk(ck,4);
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
     * 京东vipck上传
     * @param ck
     * @return
     */
    @GetMapping("/addVipCk")
    public Result addVipCk(@RequestParam String ck,@RequestParam String token) {
        log.info("京东工具人ck上传参数：{}", ck);
        if (StringUtils.isEmpty(ck)){
            return Result.error("-1","ck不能为空");
        }
        //校验token
        if (!"bienao:vip".equals(token)){
            return Result.error("-1","token校验失败");
        }

        try {
            boolean flag = ckService.addCk(ck,2);
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
     * 京东ck上传自用
     * @param ck
     * @return
     */
    @GetMapping("/addSvipCk")
    public Result addSvipCk(@RequestParam String ck,@RequestParam String token) {
        log.info("京东vipck上传参数：{}", ck);
        if (StringUtils.isEmpty(ck)){
            return Result.error("-1","ck不能为空");
        }
        //校验token
        if (!"bienao-Svip".equals(token)){
            return Result.error("-1","token校验失败");
        }

        try {
            boolean flag = ckService.addCk(ck,1);
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
}
