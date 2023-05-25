package com.bienao.robot.controller.Jetbrains;

import com.bienao.robot.annotation.PassToken;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.jetbrains.JetbrainsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/jetbrains")
public class JetbrainsController {

    @Autowired
    private JetbrainsService jetbrainsService;

    /**
     *
     * @return
     */
    @PassToken
    @GetMapping("/getValidUrls")
    public Result getValidUrls() {
        List<String> validUrl = jetbrainsService.getValidUrls();
        return Result.success(validUrl);
    }

    /**
     *
     * @return
     */
    @PassToken
    @PostMapping("/addUrls")
    public Result addUrls(@RequestBody String urls) {
        jetbrainsService.addUrls(urls);
        return Result.success();
    }
}
