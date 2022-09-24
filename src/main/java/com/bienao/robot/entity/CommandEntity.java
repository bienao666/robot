package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 命令
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandEntity {

    //序号
    private Integer id;

    //命令
    @NotBlank(message = "命令不能为空")
    @Size(max = 10, message = "命令长度不能超过10个字符")
    private String command;

    //功能
    @Size(max = 20, message = "功能长度不能超过20个字符")
    private String function;

    //回复
    @Size(max = 100, message = "回复长度不能超过100个字符")
    private String reply;

    //新增时间
    private String createdTime;

    //更新时间
    private String updatedTime;
}
