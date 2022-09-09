package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * 线报活动
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WireActivityEntity {

    //序号
    private Integer id;

    //活动内容
    @NotBlank(message = "活动内容不能为空")
    @Size(max = 500, message = "活动名称长度不能超过500个字符")
    private String content;

    //运行结果
    private String result;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;

}
