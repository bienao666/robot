package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class SystemParam {
    //序号
    private Integer id;

    //参数
    private String code;

    //参数名
    private String codeName;

    //参数值
    private String value;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;
}
