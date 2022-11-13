package com.bienao.robot.service.impl.jingdong;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.PatternConstant;
import com.bienao.robot.Socket.ZqdyjWebSocket;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdZqdyjEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.jingdong.JdZqdyjMapper;
import com.bienao.robot.service.jingdong.ZqdyjService;
import com.bienao.robot.utils.jingdong.JDUtil;
import com.bienao.robot.utils.jingdong.MakeMoneyShopUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ZqdyjServiceImpl implements ZqdyjService {

    @Autowired
    private JdZqdyjMapper jdZqdyjMapper;

    @Autowired
    private ZqdyjWebSocket zqdyjWebSocket;

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

        //判断是ck还是口令
        Matcher matcher = PatternConstant.ckPattern.matcher(param);
        String needHelpPtPin = "";
        if (matcher.find()){
            //ck
            needHelpck = param;
            needHelpPtPin = matcher.group(2);
            JSONObject info = MakeMoneyShopUtil.getInfo(param);
            if (info.getInteger("code") == 13){
                return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "账号未登陆，或者账号有误");
            }else if (info.getInteger("hot")==1){
                return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "账号火爆");
            } else {
                //助力码
                sId = info.getString("sId");
            }
        }else {
            //口令
            JSONObject res = JDUtil.parseCommand(param);
            if (0 != res.getInteger("code") || res.getJSONObject("data") == null){
                return Result.error(ErrorCodeConstant.SERVICE_ERROR, "解析口令异常");
            }
            String jumpUrl = res.getJSONObject("data").getString("jumpUrl");
            if (StringUtils.isEmpty(jumpUrl)){
                return Result.error(ErrorCodeConstant.SERVICE_ERROR, "解析口令异常");
            }
            String[] split = jumpUrl.split("&");
            for (String s : split) {
                if (s.contains("shareId")){
                    sId = s.replace("shareId=","");
                    break;
                }
            }
        }

        //获取所有 有效的 还有助力的 非火爆的ck
        List<JSONObject> zqdyjCk = jdZqdyjMapper.getZqdyjCk();
        zqdyjCk = zqdyjCk.stream().filter(jsonObject -> !"1".equals(jsonObject.getString("isHei"))&&!"0".equals(jsonObject.getString("toHelpStatus"))).collect(Collectors.toList());
        Collections.shuffle(zqdyjCk);

        if (zqdyjCk.size()==0){
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "没有可助力的账号了！！！");
        }

        Integer MaxTime = 10;
        Integer helpCount = 0;

        //开始助力
        zqdyjWebSocket.sendMessageAll("开始助力...");
        for (JSONObject jsonObject : zqdyjCk) {

            if (helpCount>=MaxTime){
                break;
            }

            Integer ckid = jsonObject.getInteger("ckid");
            String ck = jsonObject.getString("ck");

            matcher = PatternConstant.jdPinPattern.matcher(ck);
            if (matcher.find()){

                String ptPin = matcher.group(1);
                zqdyjWebSocket.sendMessageAll(ptPin+"去助力-->"+(StringUtils.isEmpty(needHelpPtPin)?sId:needHelpPtPin));

                JSONObject info = MakeMoneyShopUtil.getInfo(ck);

                if (info.getInteger("code") == 13){
                    //未登陆
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    zqdyjWebSocket.sendMessageAll(">>>>>>>>"+ptPin+"账号未登录<<<<<<<<");
                    continue;
                }

                if (info.getInteger("hot")==1){
                    //火爆
                    JdZqdyjEntity jdZqdyjEntity = new JdZqdyjEntity();
                    jdZqdyjEntity.setCkId(ckid);
                    jdZqdyjEntity.setIsHei(1);
                    jdZqdyjEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                    updateZqdyjCk(jdZqdyjEntity);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    zqdyjWebSocket.sendMessageAll(">>>>>>>>"+ptPin+"账号火爆<<<<<<<<");
                    continue;
                }

                JSONObject help = MakeMoneyShopUtil.help(sId, ck);
                if (help.getInteger("code") == 0){
                    helpCount++;
                    zqdyjWebSocket.sendMessageAll("!!!>>>>>>>>第"+helpCount+"次助力成功<<<<<<<<!!!");
                }else if (help.getInteger("code") == 1002){
                    zqdyjWebSocket.sendMessageAll(">>>>>>>>"+help.getString("msg")+"<<<<<<<<");
                    return Result.error(ErrorCodeConstant.PARAMETER_ERROR, help.getString("msg"));
                }else {
                    if (help.getInteger("nohelp")==0){
                        zqdyjWebSocket.sendMessageAll(">>>>>>>>今日无助力次数了<<<<<<<<");
//                        log.info("今日无助力次数了！");
                        JdZqdyjEntity jdZqdyjEntity = new JdZqdyjEntity();
                        jdZqdyjEntity.setCkId(ckid);
                        jdZqdyjEntity.setToHelpStatus(0);
                        jdZqdyjEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                        updateZqdyjCk(jdZqdyjEntity);
                    }else if (help.getInteger("nohelp")==1){
                        zqdyjWebSocket.sendMessageAll(">>>>>>>>已助力过TA<<<<<<<<");
                    }else if (help.getInteger("nohelp")==2){
                        zqdyjWebSocket.sendMessageAll(">>>>>>>>不能为自己助力<<<<<<<<");
                    }else if (help.getInteger("nohelp")==4){
                        zqdyjWebSocket.sendMessageAll(">>>>>>>>"+ptPin+"账号火爆<<<<<<<<");
                        JdZqdyjEntity jdZqdyjEntity = new JdZqdyjEntity();
                        jdZqdyjEntity.setCkId(ckid);
                        jdZqdyjEntity.setIsHei(1);
                        jdZqdyjEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                        updateZqdyjCk(jdZqdyjEntity);
                    }else if (help.getInteger("nohelp")==5){
                        zqdyjWebSocket.sendMessageAll(">>>>>>>>"+needHelpPtPin+"已助力满<<<<<<<<");
                        log.info("已助力满");
                        break;
                    }else {
                        log.info("助力异常：{}",help.getString("msg"));
                    }
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //领取奖励
        if (StringUtils.isNotEmpty(needHelpck)){
            List<JSONObject> taskList = MakeMoneyShopUtil.getTask(needHelpck);
            Double totalMoney = 0.00;
            if (taskList != null){
                zqdyjWebSocket.sendMessageAll("开始获取奖励...");
                for (JSONObject task : taskList) {
                    if (task.getInteger("awardStatus") != 1){
                        for (int i = 0; i < (task.getInteger("realCompletedTimes") - task.getInteger("targetTimes") + 1); i++) {
                            JSONObject res = MakeMoneyShopUtil.award(needHelpck, task.getInteger("taskId"));
                            if (res!=null && res.getInteger("code")==0){
                                Double money = res.getDouble("money");
                                log.info("获得营业金："+money+"元");
                                zqdyjWebSocket.sendMessageAll("获得营业金："+money+"元");
                                if (3533 == task.getInteger("taskId")){
                                    totalMoney += task.getInteger("completedTimes") * task.getInteger("reward") / 100.00;
                                }
                                if (3532 == task.getInteger("taskId")){
                                    totalMoney += task.getInteger("completedTimes") * task.getInteger("reward") / 100.00;
                                }
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                log.info("今日获得营业金总额："+totalMoney+"元");
                zqdyjWebSocket.sendMessageAll("今日获得营业金总额："+totalMoney+"元");
            }
        }

        return Result.success();
    }

    @Override
    public void reset() {
        jdZqdyjMapper.reset();
    }


    @Override
    public Result test(String account) {
        zqdyjWebSocket.sendMessageAll(">>>>>>>>1<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>2<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>3<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>4<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>4<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>5<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>6<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>7<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>8<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>9<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>助力成功<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>11<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>12<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>13<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>14<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>15<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>16<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>17<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>18<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>19<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>20<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>21<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>22<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>23<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>24<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>25<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>26<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>27<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>28<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>29<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>30<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>31<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>32<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>33<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>34<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>35<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>36<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>37<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>38<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>39<<<<<<<<");
        zqdyjWebSocket.sendMessageAll(">>>>>>>>40<<<<<<<<");
        return Result.success();
    }

    @Override
    public Result getZqdyjData() {
        List<JSONObject> zqdyjCk = jdZqdyjMapper.getZqdyjCk();
        JSONObject res = new JSONObject();
        //ck总数
        res.put("totalCount",zqdyjCk.size());
        //火爆
        Long hotCount = zqdyjCk.stream().filter(jsonObject -> "1".equals(jsonObject.getString("isHei"))).count();
        res.put("hotCount",hotCount);
        //无助力
        Long noHelp = zqdyjCk.stream().filter(jsonObject -> "0".equals(jsonObject.getString("toHelpStatus"))).count();
        res.put("noHelp",noHelp);
        //有助力
        Long hasHelp = zqdyjCk.stream().filter(jsonObject -> !"1".equals(jsonObject.getString("isHei"))&&!"0".equals(jsonObject.getString("toHelpStatus"))).count();
        res.put("hasHelp",hasHelp);
        return Result.success(res);
    }

    @Override
    public Result resetHot() {
        int count = jdZqdyjMapper.resetHot();
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
