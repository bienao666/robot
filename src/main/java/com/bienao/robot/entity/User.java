package com.bienao.robot.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
public class User {
    //序号
    private Integer id;

    //用户名
    private String userName;

    //密码
    private String passWord;

    //微信uid
    private String wxid;

    //微信名称
    private String wxName;

    private String jdPtPin = "";

    private List<String> jdPtPinList;

    private String wxpusheruid;

    //所在城市
    private String city;

    //ip地址
    private String ip;

    //功能类型
    private Integer functionType;

    //用户状态 0(正常)，1(拉黑)
    private Integer status;

    //用户等级
    private Integer level;

    //新增时间
    private String createdTime;

    //更新时间
    private String updatedTime;

    public List<String> getJdPtPinList() {
        List<String> jdPtPinList = new ArrayList<>();
        if (StringUtils.isNotEmpty(jdPtPin)){
            jdPtPinList = Arrays.asList(jdPtPin.split("#"));
        }
        return jdPtPinList;
    }

    public String getJdPtPin() {
        if (jdPtPinList!=null && jdPtPinList.size()>0){
             return String.join("#",jdPtPinList);
        }
        return jdPtPin;
    }

}
