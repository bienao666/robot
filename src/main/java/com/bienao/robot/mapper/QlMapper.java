package com.bienao.robot.mapper;

import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QlMapper {

    /**
     * 添加青龙
     * @param ql
     * @return
     */
    int addQl(QlEntity ql);

    /**
     * 查询青龙
     * @param url
     * @return
     */
    QlEntity queryQlByUrl(String url);

    QlEntity queryQl(QlEntity qlEntity);

    /**
     * 查询青龙
     * @return
     */
    List<QlEntity> queryQls(List<Integer> ids);

    /**
     * 修改青龙
     * @param ql
     * @return
     */
    int updateQl(QlEntity ql);

    /**
     * 删除青龙
     * @return
     */
    int deleteQls(List<Integer> ids);

}
