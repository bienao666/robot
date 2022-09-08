package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 线报关键字
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WireKeyEntity {

    //序号
    private Integer id;

    //活动id
    private Integer wireId;

    //洞察变量
    @NotBlank(message = "洞察变量不能为空")
    @Size(max = 30, message = "洞察变量长度不能超过50个字符")
    private String key;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;

    public WireKeyEntity(Integer wireid, String key) {
        this.wireId = wireId;
        this.key = key;
    }
}
