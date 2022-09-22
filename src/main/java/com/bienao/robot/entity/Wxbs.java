package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信步数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wxbs {
    //序号
    private Integer id;

    //用户名
    private String userName;

    //密码
    private String passWord;

    //最小步数
    private Integer minstep;

    //最大步数
    private Integer maxstep;

    //新增时间
    private String createdTime;

    //过期时间
    private String expiryTime;

    //更新时间
    private String updatedTime;
}
