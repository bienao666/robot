package com.bienao.robot.controller.version;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.User;
import com.bienao.robot.service.version.VersionService;
import com.bienao.robot.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版本管理
 *
 * @author tiandawei
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/version")
public class VersionController {

    @Autowired
    private VersionService versionService;

    /**
     * 查询版本
     * @return
     */
    @PassToken
    @GetMapping("/queryVersion")
    public Result queryVersion() {
        return Result.success(versionService.queryVersion());
    }

    /**
     * 查询最新版本
     * @return
     */
    @PassToken
    @GetMapping("/queryNewestVersion")
    public Result queryNewestVersion() {
        JSONObject newestVersion = versionService.queryNewestVersion();
        return Result.success(newestVersion);
    }
}
