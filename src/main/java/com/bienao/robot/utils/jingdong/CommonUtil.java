package com.bienao.robot.utils.jingdong;


import com.bienao.robot.entity.jingdong.JdCkEntity;

import java.util.Collections;
import java.util.List;

public class CommonUtil {
    /**
     * 按照助力规则排序
     * @param jdCks
     * @param vipCks
     * @param ptCks
     * @return
     */
    public static List<JdCkEntity> getHelpCks(List<JdCkEntity> jdCks, List<JdCkEntity> vipCks, List<JdCkEntity> ptCks){
        //随机排序
        Collections.shuffle(vipCks);
        List<JdCkEntity> vipCks1 = vipCks.subList(0,vipCks.size() / 4);
        List<JdCkEntity> vipCks2 = vipCks.subList(vipCks.size() / 4,vipCks.size());
        //添加vipCk第一部分
        jdCks.addAll(vipCks1);
        //随机排序
        Collections.shuffle(ptCks);
        List<JdCkEntity> ptCks1 = ptCks.subList(0,ptCks.size() / 4);
        List<JdCkEntity> ptCks2 = ptCks.subList(ptCks.size() / 4,ptCks.size());
        //添加ck第一部分
        jdCks.addAll(ptCks1);

        //添加剩下的部分
        jdCks.addAll(vipCks2);
        jdCks.addAll(ptCks2);
        return jdCks;
    }
}
