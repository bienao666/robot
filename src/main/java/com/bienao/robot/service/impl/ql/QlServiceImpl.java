package com.bienao.robot.service.impl.ql;

import cn.hutool.cache.Cache;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.weixin.QingLongGuanLiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class QlServiceImpl implements QlService {

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private QingLongGuanLiUtil qingLongGuanLiUtil;

    @Autowired
    private QlMapper qlMapper;

    private Cache<String, String> redis = WXConstant.redis;

    /**
     * 一键设置大车头
     * @return
     */
    @Override
    public boolean oneKeyBigHead(JSONObject content) {
        JSONObject qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
        String bigHead = qlgl.getString("bigHead");

        return true;
    }

    /**
     * 添加青龙
     * @param ql
     */
    @Override
    public Result addQl(JSONObject ql){
        String url = ql.getString("url");
        if (StringUtils.isEmpty(url)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址不能为空");
        }
        if (url.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址长度异常");
        }
        String clientID = ql.getString("clientID");
        if (StringUtils.isEmpty(clientID)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID不能为空");
        }
        if (clientID.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID长度异常");
        }
        String clientSecret = ql.getString("clientSecret");
        if (StringUtils.isEmpty(clientSecret)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret不能为空");
        }
        if (clientSecret.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret长度异常");
        }

        JSONObject result = qlMapper.queryQlByUrl(url);
        if (result == null){
            int i = qlMapper.addQl(ql);
            if (i==1){
                return Result.success("添加成功");
            }else {
                return Result.error(ErrorCodeConstant.QINGLONG_ADD_ERROR,"青龙添加失败");
            }
        }else {
            return Result.error(ErrorCodeConstant.QINGLONG_EXSIT_ERROR,"青龙面板已存在");
        }
    }

    /**
     * 查询所有青龙
     * @return
     */
    @Override
    public Result queryQls(String id){
        List<JSONObject> qls = qlMapper.queryQls(id);
        for (JSONObject ql : qls) {
            JSONObject jsonObject = qlUtil.getToken(ql.getString("url"), ql.getString("clientID"), ql.getString("clientSecret"));
            if (jsonObject == null) {
                ql.put("status","异常");
            }else {
                ql.put("status","正常");
            }
        }
        return Result.success(qls);
    }

    /**
     * 更新青龙
     * @return
     */
    @Override
    public Result updateQl(JSONObject ql){
        String id = ql.getString("id");
        if (StringUtils.isEmpty(id)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"id不能为空");
        }
        if (id.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"id长度异常");
        }
        String url = ql.getString("url");
        if (StringUtils.isEmpty(url)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址不能为空");
        }
        if (url.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址长度异常");
        }
        String clientID = ql.getString("clientID");
        if (StringUtils.isEmpty(clientID)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID不能为空");
        }
        if (clientID.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID长度异常");
        }
        String clientSecret = ql.getString("clientSecret");
        if (StringUtils.isEmpty(clientSecret)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret不能为空");
        }
        if (clientSecret.length()>20){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret长度异常");
        }

        ql.put("updatedTime",new Date());

        int i = qlMapper.updateQl(ql);
        if (i==1){
            return Result.success("修改成功");
        }else {
            return Result.error(ErrorCodeConstant.QINGLONG_UPDATE_ERROR,"青龙修改失败");
        }
    }

    /**
     * 查询脚本
     * @return
     */
    public Result queryScripts(){
        HashSet<JSONObject> scripts = new HashSet<>();
        List<JSONObject> qls = qlMapper.queryQls(null);
        for (JSONObject ql : qls) {
            try {
                String url = ql.getString("url");
                String clientId = ql.getString("clientId");
                String clientSecret = ql.getString("clientSecret");
                JSONObject tokenJson = qlUtil.getToken(url, clientId, clientSecret);
                String token = tokenJson.getString("token");
                String tokenType = tokenJson.getString("token_type");
                List<JSONObject> crons = qlUtil.getCrons(url, tokenType, token);
            } catch (Exception e) {
                log.error("查询脚本失败",e);
            }
        }
        return Result.success();
    }
}
