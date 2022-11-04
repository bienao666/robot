package com.bienao.robot.entity.jingdong;

import lombok.Data;

/**
 * 赚钱大赢家
 */
@Data
public class JdZqdyjEntity {
    //序号
    private Integer id;

    //互助码
    private String helpCode;

    //ckid
    private Integer ckId;

    //是否黑了 0否1是
    private Integer isHei;

    //是否助力满 0否1是
    private Integer helpStatus;

    //是否还有助力 0否1是
    private Integer toHelpStatus;

    //新增时间
    private String createdTime;

    //更新时间
    private String updatedTime;
}
