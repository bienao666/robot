package com.bienao.robot.entity;

import lombok.Data;

import java.util.List;

@Data
public class QlEnv {
    private Integer id;
    //变量值
    private String value;
    private String timestamp;
    //0启用 1禁用
    private Integer status;
    private String position;
    //变量名
    private String name;
    //备注
    private String remarks;
    private String createdAt;
    private String updatedAt;
    //所属青龙id
    private Integer qlId;
    //所属青龙位置
    private Integer qlIndex;
}
