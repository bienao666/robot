package com.bienao.robot.service.impl.jingdong;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.PatternConstant;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdZqdyjEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.jingdong.JdZqdyjMapper;
import com.bienao.robot.service.jingdong.ZqdyjService;
import com.bienao.robot.utils.jingdong.MakeMoneyShopUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

@Slf4j
@Service
public class ZqdyjServiceImpl implements ZqdyjService {

    @Autowired
    private JdZqdyjMapper jdZqdyjMapper;

    /**
     * 赚钱大赢家助力
     * @param param
     * @return
     */
    @Override
    public Result help(String param) {
        //ck
        String needHelpck = "";
        //助力码
        String sId ="";

        //判断是ck还是助力码
        Matcher matcher = PatternConstant.ckPattern.matcher(param);
        String needHelpPtPin = "";
        if (matcher.find()){
            needHelpck = param;
            needHelpPtPin = matcher.group(2);
            JSONObject info = MakeMoneyShopUtil.getInfo(param);
            if (info.getInteger("hot")==1){
                return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "账号火爆");
            }else {
                //助力码
                sId = info.getString("sId");
            }
        }else {
            sId = param;
        }

        //获取所有 有效的 还有助力的 非火爆的ck
        List<JSONObject> zqdyjCk = jdZqdyjMapper.getZqdyjCk();

        Integer MaxTime = 10;
        Integer helpCount = 0;

        //开始助力
        for (JSONObject jsonObject : zqdyjCk) {

            if (helpCount>MaxTime){
                break;
            }

            if ("0".equals(jsonObject.getString("isHei"))){
                //火爆
                continue;
            }

            if (0 == jsonObject.getInteger("toHelpStatus")){
                //无助力次数
                continue;
            }

            Integer ckid = jsonObject.getInteger("ckid");
            String ck = jsonObject.getString("ck");

            JSONObject info = MakeMoneyShopUtil.getInfo(param);
            if (info.getInteger("hot")==0){
                //火爆
                JdZqdyjEntity jdZqdyjEntity = new JdZqdyjEntity();
                jdZqdyjEntity.setCkId(ckid);
                jdZqdyjEntity.setIsHei(1);
                jdZqdyjEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                updateZqdyjCk(jdZqdyjEntity);
            }

            matcher = PatternConstant.jdPinPattern.matcher(ck);
            if (matcher.find()){
                String ptPin = matcher.group(1);
                log.info(ptPin+"去助力-->"+needHelpPtPin);
                JSONObject help = MakeMoneyShopUtil.help(sId, ck);
                if (help.getInteger("code") == 0){
                    log.info("助力成功");
                    helpCount++;
                }else {
                    if (help.getInteger("nohelp")==0){
                        log.info("今日无助力次数了！");
                        JdZqdyjEntity jdZqdyjEntity = new JdZqdyjEntity();
                        jdZqdyjEntity.setCkId(ckid);
                        jdZqdyjEntity.setToHelpStatus(0);
                        jdZqdyjEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                        updateZqdyjCk(jdZqdyjEntity);
                    }else if (help.getInteger("nohelp")==1){
                        log.info("已助力过TA");
                    }else if (help.getInteger("nohelp")==2){
                        log.info("不能为自己助力");
                    }else {
                        log.info("助力异常：{}",help.getShort("msg"));
                    }
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //领取奖励
        if (StringUtils.isNotEmpty(needHelpck)){
            List<JSONObject> taskList = MakeMoneyShopUtil.getTask(needHelpck);
            if (taskList != null){
                for (JSONObject task : taskList) {
                    if (task.getLong("completedTimes") < task.getLong("realCompletedTimes")){
                        String msg = MakeMoneyShopUtil.award(needHelpck, task.getInteger("taskId"));
                        log.info(msg);
                    }
                }
            }
        }

        return Result.success();
    }

    /**
     * 修改赚钱大赢家账号
     * @param jdZqdyjEntity
     */
    private void updateZqdyjCk(JdZqdyjEntity jdZqdyjEntity){
        int i = jdZqdyjMapper.updateByCkId(jdZqdyjEntity);
        if (i == 0){
            //不存在 新加
            jdZqdyjMapper.add(jdZqdyjEntity);
        }
    }
}
