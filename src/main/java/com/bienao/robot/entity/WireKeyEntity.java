package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    //活动名称
    private Integer wireid;

    //洞察变量
    private String key;

    //新增时间
    private Date createdTime;

    //更新时间
    private Date updatedTime;

    public WireKeyEntity(Integer wireid, String key) {
        this.wireid = wireid;
        this.key = key;
    }
}
