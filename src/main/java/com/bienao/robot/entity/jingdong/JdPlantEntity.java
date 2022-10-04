package com.bienao.robot.entity.jingdong;

import lombok.Data;

@Data
public class JdPlantEntity {
    //序号
    private Integer id;

    //种豆得豆互助码
    private String helpCode;

    //ckid
    private Integer ckId;

    //种豆得豆是否黑了 0否1是
    private Integer isPlantHei;

    //是否助力满 0否1是
    private Integer helpStatus;

    //是否还有助力 0否1是
    private Integer toHelpStatus;

    //助力次数
    private Integer times = 0;
}
