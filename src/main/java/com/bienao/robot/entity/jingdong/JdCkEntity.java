package com.bienao.robot.entity.jingdong;

import lombok.Data;

import java.util.Date;

@Data
public class JdCkEntity {
    //序号
    private Integer id;

    //京东ck
    private String ck;

    //pt_pin
    private String ptPin;

    //过期天数
    private Integer expiry;

    //京东ck备注
    private String remark;

    //svip 0，vip 1，普通用户 2，工具人 3
    private Integer level;

    //是否有效 0否1是
    private Integer status;

    //当日京豆收益
    private Integer jd;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;

    //东东农场
    private JdFruitEntity jdFruitEntity;

    //东东萌宠
    private JdPetEntity jdPetEntity;

    //种豆得豆
    private JdPlantEntity jdPlantEntity;
}
