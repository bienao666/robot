package com.bienao.robot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Group {
    //序号
    private Integer id;

    //群号id
    private String groupid;

    //微信群名称
    private String groupName;

    //功能类型
    private Integer functionType;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;
}
