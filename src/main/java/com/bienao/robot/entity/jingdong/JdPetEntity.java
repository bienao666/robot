package com.bienao.robot.entity.jingdong;

import lombok.Data;

@Data
public class JdPetEntity {
    //序号
    private Integer id;

    //东东萌宠互助码
    private String helpCode;

    //ckid
    private Integer ckId;

    //东东萌宠是否黑了 0否1是
    private Integer isPetHei;

    //是否助力满 0否1是
    private Integer helpStatus;

    //是否还有助力 0否1是
    private Integer toHelpStatus;
}
