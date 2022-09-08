package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * 线报
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WireEntity {

    //序号
    private Integer id;

    //活动名称
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 30, message = "活动名称长度不能超过30个字符")
    private String activityName;

    //脚本名称
    @NotBlank(message = "脚本名称不能为空")
    @Size(max = 30, message = "脚本名称长度不能超过50个字符")
        private String script;

    //洞察变量
    private List<WireKeyEntity> keys;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;

    public WireEntity(String activityName, String script) {
        this.activityName = activityName;
        this.script = script;
    }
}
