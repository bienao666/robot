package com.bienao.robot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    //序号
    private Integer id;

    //微信uid
    private String wxid;

    //微信名称
    private String wxName;

    //所在城市
    private String city;

    //ip地址
    private String ip;

    //功能类型
    private String functionType;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;
}
