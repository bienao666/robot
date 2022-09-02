package com.bienao.robot.service.impl.ql;

import cn.hutool.cache.Cache;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.QlEntity;
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
    public Result addQl(QlEntity ql){
        String url = ql.getUrl();
        if (StringUtils.isEmpty(url)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址不能为空");
        }
        if (url.length()>50){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址长度异常");
        }
        String clientID = ql.getClientID();
        if (StringUtils.isEmpty(clientID)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID不能为空");
        }
        if (clientID.length()>50){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID长度异常");
        }
        String clientSecret = ql.getClientSecret();
        if (StringUtils.isEmpty(clientSecret)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret不能为空");
        }
        if (clientSecret.length()>50){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret长度异常");
        }

        QlEntity result = qlMapper.queryQlByUrl(url);
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
        List<QlEntity> qls = qlMapper.queryQls(id);
        for (QlEntity ql : qls) {
            JSONObject jsonObject = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
            if (jsonObject == null) {
                ql.setStatus("异常");
            }else {
                ql.setStatus("正常");
            }
        }
        return Result.success(qls);
    }

    /**
     * 更新青龙
     * @return
     */
    @Override
    public Result updateQl(QlEntity ql){
        Integer id = ql.getId();
        if (id == null){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"id不能为空");
        }
        String url = ql.getUrl();
        if (StringUtils.isEmpty(url)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址不能为空");
        }
        if (url.length()>50){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"地址长度异常");
        }
        String clientID = ql.getClientID();
        if (StringUtils.isEmpty(clientID)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID不能为空");
        }
        if (clientID.length()>50){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientID长度异常");
        }
        String clientSecret = ql.getClientSecret();
        if (StringUtils.isEmpty(clientSecret)){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret不能为空");
        }
        if (clientSecret.length()>50){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR,"clientSecret长度异常");
        }

        ql.setUpdatedTime(new Date());

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
    @Override
    public Result queryScripts(){
        HashSet<JSONObject> scripts = new HashSet<>();
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            try {
                String url = ql.getUrl();
                String clientId = ql.getClientID();
                String clientSecret = ql.getClientSecret();
                JSONObject tokenJson = qlUtil.getToken(url, clientId, clientSecret);
                String token = tokenJson.getString("token");
                String tokenType = tokenJson.getString("token_type");
                List<JSONObject> crons = qlUtil.getCrons(url, tokenType, token);
                if (crons==null){
                    return Result.error(ErrorCodeConstant.INTERFACE_CALL_ERROR,"接口调用失败");
                }
                for (JSONObject cron : crons) {
                    JSONObject script = new JSONObject();
                    script.put("name",cron.getString("name"));
                    String command = cron.getString("command").replace("task ","");
                    if (command.contains("/")){
                        command = command.split("/")[1];
                    }
                    script.put("command",command);
                    scripts.add(script);
                }
            } catch (Exception e) {
                log.error("查询脚本失败",e);
            }
        }
        return Result.success(scripts);
    }
}
