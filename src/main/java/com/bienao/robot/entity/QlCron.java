package com.bienao.robot.entity;

import lombok.Data;

import java.util.List;

@Data
public class QlCron {
    private Integer id;
    private String name;
    private String command;
    private String schedule;
    private String timestamp;
    private boolean saved;
    //任务状态 0(执行中) 1(未执行)
    private Integer status;
    private Integer isSystem;
    private Integer pid;
    private Integer isDisabled;
    private Integer isPinned;
    private String log_path;
    private List<String> labels;
    private Integer last_running_time;
    private Integer last_execution_time;
    private String createdAt;
    private String updatedAt;
}
