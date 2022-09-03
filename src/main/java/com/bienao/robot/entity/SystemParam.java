package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemParam {
    //序号
    private Integer id;

    //参数
    private String code;

    //参数名
    private String codeName;

    //参数值
    private String value;

    //是否展示
    private Integer isShow;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;
}
