package com.bienao.robot.entity.jingdong;

import lombok.Data;

@Data
public class JdFruitEntity {
    //序号
    private Integer id;

    //东东农场互助码
    private String helpCode;

    //ckid
    private Integer ckId;

    //东东农场是否黑了 0否1是
    private Integer isFruitHei;

    //是否助力满 0否1是
    private Integer helpStatus;

    //是否还有助力 0否1是
    private Integer toHelpStatus;

    //天天抽奖是否助力满 0否1是
    private Integer helpLotteryStatus;

    //天天抽奖是否还有助力 0否1是
    private Integer toHelpLotteryStatus;

    //助力次数
    private Integer times = 0;
}
