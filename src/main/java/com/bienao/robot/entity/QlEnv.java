package com.bienao.robot.entity;

import lombok.Data;

import java.util.List;

@Data
public class QlEnv {
    private Integer id;
    //变量值
    private String value;
    private String timestamp;
    private Integer status;
    private String position;
    //变量名
    private String name;
    //备注
    private String remarks;
    private String createdAt;
    private String updatedAt;
}
