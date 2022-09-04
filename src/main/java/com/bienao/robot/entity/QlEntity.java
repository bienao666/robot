package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QlEntity {

    //序号
    private Integer id;

    //青龙地址
    private String url;

    //clientID
    private String clientID;

    //clientSecret
    private String clientSecret;

    //车头
    private String head;

    //备注
    private String remark;

    //青龙状态
    private String status;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;
}