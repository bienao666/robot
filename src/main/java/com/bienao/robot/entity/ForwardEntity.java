package com.bienao.robot.entity;

import lombok.Data;

@Data
public class ForwardEntity {

    //序号
    private Integer id;

    //消息来源
    private String from;

    //消息来源名称
    private String fromName;

    //消息来源类型
    private Integer fromtype;

    //消息转发
    private String to;

    //消息转发名称
    private String toName;

    //消息转发类型
    private Integer totype;

    //新增时间
    private String createdTime;

    //更新时间
    private String updatedTime;
}
