package com.bienao.robot.entity.jingdong;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class JdCkEntity {
    //序号
    private Integer id;

    //京东ck
    @NotBlank(message = "京东ck不能为空")
    private String ck;

    //pt_pin
    private String ptPin;

    //京东ck备注
    private String remark;

    //svip 0，vip 1，普通用户 2，工具人 3
    @NotNull(message = "等级不能为空")
    private Integer level;

    //是否有效 0否1是
    private Integer status;

    //当日京豆收益
    private Integer jd;

    //新增时间
    private String createdTime;

    //过期时间
    private String expiryTime;

    //更新时间
    private String updatedTime;

    //东东农场
    private JdFruitEntity jdFruitEntity;

    //东东萌宠
    private JdPetEntity jdPetEntity;

    //种豆得豆
    private JdPlantEntity jdPlantEntity;
}
