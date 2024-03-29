package com.bienao.robot.service.impl.jingdong;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.QlEnv;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.entity.jingdong.JdFruitEntity;
import com.bienao.robot.entity.jingdong.JdPetEntity;
import com.bienao.robot.entity.jingdong.JdPlantEntity;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.mapper.jingdong.*;
import com.bienao.robot.redis.Redis;
import com.bienao.robot.service.jingdong.JdService;
import com.bienao.robot.utils.jingdong.GetUserAgentUtil;
import com.bienao.robot.utils.jingdong.JdBeanChangeUtil;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JdServiceImpl implements JdService {

    @Value("${JD_API_HOST}")
    private String JDAPIHOST;

    @Autowired
    private JdCkMapper jdCkMapper;

    @Autowired
    private JdFruitMapper jdFruitMapper;

    @Autowired
    private JdPetMapper jdPetMapper;

    @Autowired
    private JdPlantMapper jdPlantMapper;

    @Autowired
    private JdJdMapper jdJdMapper;

    @Autowired
    private QlMapper qlMapper;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private JdZqdyjMapper jdZqdyjMapper;

    private Cache<String, String> redis = Redis.redis;

    private Pattern jdPinPattern = Pattern.compile("pt_pin=(.+?);");

    /**
     * 东东农场互助
     */
    @Async("asyncServiceExecutor")
    @Override
    public void fruitShareHelp(List<JdCkEntity> cks, int zlcwaittime) {
        //判断是否有锁
        String fruitShareHelpIng = redis.get("fruitShareHelp",false);
        if (StringUtils.isEmpty(fruitShareHelpIng)) {
            //加锁
            redis.put("fruitShareHelp", "true", (zlcwaittime * 3L) * 1000);
        } else {
            //有锁
            log.info("东东农场互助进行中。。。");
            return;
        }

        //需要被助力的ck集合
        List<JdCkEntity> jdCks = new ArrayList<>();
        //查询所有的svipck 未助力满
        List<JdCkEntity> svipCks = new ArrayList<>();
        //查询所有的vipck 未助力满
        List<JdCkEntity> vipCks = new ArrayList<>();
        //查询所有普通用户的ck 未助力满
        List<JdCkEntity> ptCks = new ArrayList<>();
        //有助力的ck集合
        List<JdCkEntity> toHelpJdCks = new ArrayList<>();

        for (JdCkEntity ck : cks) {
            if (ck.getStatus() == 1) {
                //ck失效
                continue;
            }
            JdFruitEntity jdFruitEntity = ck.getJdFruitEntity();
            if (jdFruitEntity == null){
                jdFruitEntity = new JdFruitEntity();
                jdFruitEntity.setCkId(ck.getId());
                jdFruitEntity.setIsFruitHei(0);
                jdFruitEntity.setHelpStatus(0);
                jdFruitEntity.setToHelpStatus(1);
                jdFruitEntity.setTimes(0);
                ck.setJdFruitEntity(jdFruitEntity);
                jdFruitMapper.addJdFruit(jdFruitEntity);
            }
            if (jdFruitEntity.getIsFruitHei() == 0) {
                //东东农场不黑
                if ((ck.getLevel() == 0) && (jdFruitEntity.getHelpStatus() == 0)) {
                    //自己 && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdFruitEntity.getHelpStatus() == 0)) {
                    //vip && 未助力满 && 互助码不为空
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdFruitEntity.getHelpStatus() == 0)) {
                    //普通用户 && 未助力满 && 互助码不为空
                    ptCks.add(ck);
                }
                if (jdFruitEntity.getToHelpStatus() == 1) {
                    //还有助力
                    toHelpJdCks.add(ck);
                }
            }
        }

        jdCks.addAll(svipCks);
        jdCks.addAll(vipCks);
        Collections.shuffle(ptCks);
        jdCks.addAll(ptCks);

        Collections.shuffle(toHelpJdCks);

        log.info("查询到东东农场需要被助力的ck：{}个", jdCks.size());
        log.info("查询到东东农场可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("东东农场助力开始。。。");

        Integer limit = null;
        String ddncHelpStr = systemParamUtil.querySystemParam("DDNCHELP");
        if (StringUtils.isNotEmpty(ddncHelpStr)) {
            limit = Integer.parseInt(ddncHelpStr);
            log.info("查询到东东农场助力上限：{}个", limit);
        }

        //ip黑了
        boolean isBreak = false;

        for (JdCkEntity jdCk : jdCks) {
            toHelpJdCks = toHelpJdCks.stream().filter(ck -> (ck.getStatus() == 0) && (ck.getJdFruitEntity() == null || (ck.getJdFruitEntity().getIsFruitHei()) == 0 && ck.getJdFruitEntity().getToHelpStatus() == 1)).collect(Collectors.toList());
            int count = toHelpJdCks.size();
            if (count == 0){
                log.info("东东农场无可助力账号，结束");
                return;
            }
            log.info("东东农场可以去助力的ck还有：{}个", count);
            if (isBreak){
                //ip黑了
                break;
            }

            if (limit != null) {
                int fruitHelp = jdFruitMapper.getHelpTimes();
                if (fruitHelp >= limit) {
                    return;
                }
            }

            JdFruitEntity jdFruitEntity = jdCk.getJdFruitEntity();
            if (jdFruitEntity.getHelpStatus() == 1) {
                //当前账号已满助力，跳过当前循环
                log.info("{}东东农场已助力满!!!", jdCk.getRemark());
                continue;
            }
            if (jdFruitEntity.getIsFruitHei() == 1) {
                //东东农场火爆
                log.info(jdCk.getPtPin() + "东东农场火爆1。。。");
                continue;
            }
            if (StringUtils.isEmpty(jdFruitEntity.getHelpCode())){
                JSONObject farmInfo = JdBeanChangeUtil.getjdfruit(jdCk.getCk());
                helpWait("fruitShareHelp","东东农场" ,zlcwaittime);
                if (farmInfo == null){
                    //东东农场火爆
                    log.info(jdCk.getPtPin() + "东东农场火爆2。。。");
                    jdFruitEntity.setIsFruitHei(1);
                    jdFruitMapper.updateJdFruit(jdFruitEntity);
                    continue;
                }else if(403 == farmInfo.getInteger("code")){
                    log.info("ip已黑。。。");
                    break;
                }else if(3 == farmInfo.getInteger("code")){
                    log.info(jdCk.getPtPin() + "账号过期。。。");
                    continue;
                }else if(farmInfo.getJSONObject("farmUserPro") == null){
                    //东东农场火爆
                    log.info(jdCk.getPtPin() + "东东农场火爆3。。。");
                    jdFruitEntity.setIsFruitHei(1);
                    jdFruitMapper.updateJdFruit(jdFruitEntity);
                    continue;
                }else {
                    JSONObject farmUserPro = farmInfo.getJSONObject("farmUserPro");
                    //互助码
                    String fruitShareCode = farmUserPro.getString("shareCode");
                    jdFruitEntity.setHelpCode(fruitShareCode);
                    jdFruitMapper.updateJdFruit(jdFruitEntity);
                }
            }
            log.info("东东农场开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                try {
                    JdFruitEntity toHelpJdFruitEntity = toHelpJdCk.getJdFruitEntity();
                    if (toHelpJdFruitEntity.getToHelpStatus() != 1) {
                        continue;
                    }
                    if (jdFruitEntity.getHelpStatus() == 1) {
                        //当前账号已满助力，跳过当前循环
                        log.info("{}东东农场已助力满!!!", jdCk.getRemark());
                        break;
                    }
                    if (jdCk.getCk().equals(toHelpJdCk.getCk())) {
                        //不能为自己助力
                        continue;
                    }
                    if (toHelpJdFruitEntity.getIsFruitHei() == 1) {
                        //东东农场火爆
                        log.info(toHelpJdCk.getPtPin() + "东东农场火爆3。。。");
                        continue;
                    }else {
                        JSONObject farmInfo = JdBeanChangeUtil.getjdfruit(toHelpJdCk.getCk());
                        helpWait("fruitShareHelp","东东农场" ,zlcwaittime);
                        if (farmInfo == null){
                            //东东农场火爆
                            log.info(toHelpJdCk.getPtPin() + "东东农场火爆4。。。");
                            toHelpJdFruitEntity.setIsFruitHei(1);
                            jdFruitMapper.updateJdFruit(toHelpJdFruitEntity);
                            continue;
                        }else if(3 == farmInfo.getInteger("code")){
                            toHelpJdCk.setStatus(1);
                            toHelpJdCk.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                            jdCkMapper.updateCk(toHelpJdCk);
                            log.info(jdCk.getPtPin() + "账号过期。。。");
                            continue;
                        }else if(403 == farmInfo.getInteger("code")){
                            log.info("ip已黑。。。");
                            isBreak = true;
                            break;
                        }else if(farmInfo.getJSONObject("farmUserPro") == null){
                            //东东农场火爆
                            log.info(toHelpJdCk.getPtPin() + "东东农场火爆5。。。");
                            toHelpJdFruitEntity.setIsFruitHei(1);
                            jdFruitMapper.updateJdFruit(toHelpJdFruitEntity);
                            continue;
                        }
                    }
                    log.info("{}开始准备助力东东农场->{}", toHelpJdCk.getPtPin(), jdCk.getPtPin());
                    //助力
                    helpFruit(toHelpJdCk, jdCk);
                    helpWait("fruitShareHelp","东东农场" ,zlcwaittime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("东东农场助力结束。。。");
    }

    /**
     *
     */
    public void helpWait(String type, String name, int zlcwaittime){
        //加锁
        redis.put(type, "true", (zlcwaittime * 3L) * 1000);
        try {
            log.info(name + "助力休息" + zlcwaittime + "s防止黑ip...");
            Thread.sleep(zlcwaittime * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 东东萌宠互助
     */
    @Async("asyncServiceExecutor")
    @Override
    public void petShareHelp(List<JdCkEntity> cks, int zlcwaittime) {
        //判断是否有锁
        String fruitShareHelpIng = redis.get("petShareHelp",false);
        if (StringUtils.isEmpty(fruitShareHelpIng)) {
            //加锁
            redis.put("petShareHelp", "true", (zlcwaittime * 3L) * 1000);
        } else {
            //有锁
            log.info("东东萌宠互助进行中。。。");
            return;
        }

        //需要被助力的ck集合
        List<JdCkEntity> jdCks = new ArrayList<>();
        //查询所有的svipck 未助力满
        List<JdCkEntity> svipCks = new ArrayList<>();
        //查询所有的vipck 未助力满
        List<JdCkEntity> vipCks = new ArrayList<>();
        //查询所有普通用户的ck 未助力满
        List<JdCkEntity> ptCks = new ArrayList<>();
        //有助力的ck集合
        List<JdCkEntity> toHelpJdCks = new ArrayList<>();

        for (JdCkEntity ck : cks) {
            if (ck.getStatus() == 1) {
                //ck失效
                continue;
            }
            JdPetEntity jdPetEntity = ck.getJdPetEntity();
            if (jdPetEntity == null){
                jdPetEntity = new JdPetEntity();
                jdPetEntity.setCkId(ck.getId());
                jdPetEntity.setIsPetHei(0);
                jdPetEntity.setHelpStatus(0);
                jdPetEntity.setToHelpStatus(1);
                jdPetEntity.setTimes(0);
                ck.setJdPetEntity(jdPetEntity);
                jdPetMapper.addJdPet(jdPetEntity);
            }
            if (jdPetEntity.getIsPetHei() == 0) {
                //东东萌宠不黑
                if ((ck.getLevel() == 0) && (jdPetEntity.getHelpStatus() == 0)) {
                    //自己 && 未助力满
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdPetEntity.getHelpStatus() == 0)) {
                    //vip && 未助力满
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdPetEntity.getHelpStatus() == 0)) {
                    //普通用户 && 未助力满
                    ptCks.add(ck);
                }
                if (jdPetEntity.getToHelpStatus() == 1) {
                    //还有助力
                    toHelpJdCks.add(ck);
                }
            }
        }

        jdCks.addAll(svipCks);
        jdCks.addAll(vipCks);
        Collections.shuffle(ptCks);
        jdCks.addAll(ptCks);

        Collections.shuffle(toHelpJdCks);

        log.info("查询到东东萌宠需要被助力的ck：{}个", jdCks.size());
        log.info("查询到东东萌宠可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("东东萌宠助力开始。。。");

        Integer limit = null;
        String ddmcHelpStr = systemParamUtil.querySystemParam("DDMCHELP");
        if (StringUtils.isNotEmpty(ddmcHelpStr)) {
            limit = Integer.parseInt(ddmcHelpStr);
            log.info("查询到东东萌宠助力上限：{}个", limit);
        }

        for (JdCkEntity jdCk : jdCks) {
            toHelpJdCks = toHelpJdCks.stream().filter(ck -> (ck.getStatus() == 0) && (ck.getJdPetEntity() == null || (ck.getJdPetEntity().getIsPetHei()) == 0 && ck.getJdPetEntity().getToHelpStatus() == 1)).collect(Collectors.toList());
            int count = toHelpJdCks.size();
            if (count == 0){
                log.info("东东萌宠无可助力账号，结束");
                return;
            }

            log.info("东东萌宠可以去助力的ck还有：{}个", count);

            if (limit != null) {
                int petTimes = jdPetMapper.getHelpTimes();
                if (petTimes >= limit) {
                    return;
                }
            }

            JdPetEntity jdPetEntity = jdCk.getJdPetEntity();
            if (jdPetEntity.getHelpStatus() == 1) {
                //当前账号已满助力，跳过当前循环
                log.info("{}东东萌宠已助力满!!!", jdCk.getRemark());
                continue;
            }
            if (jdPetEntity.getIsPetHei() == 1) {
                //东东萌宠火爆
                continue;
            }
            if (StringUtils.isEmpty(jdPetEntity.getHelpCode())){
                JSONObject petInfo = JdBeanChangeUtil.petRequest("initPetTown",jdCk.getCk());
                helpWait("petShareHelp","东东萌宠" ,zlcwaittime);
                if ("1018".equals(petInfo.getString("resultCode")) || "1019".equals(petInfo.getString("resultCode")) || "410".equals(petInfo.getString("resultCode"))){
                    //东东东东萌宠
                    jdPetEntity.setIsPetHei(1);
                    jdPetMapper.updateJdPet(jdPetEntity);
                    continue;
                }else {
                    JSONObject petUserPro = petInfo.getJSONObject("result");
                    if (petUserPro == null
                            || "0".equals(petUserPro.getString("userStatus"))
                            || "0".equals(petUserPro.getString("petStatus"))
                            || "0".equals(petUserPro.getString("petStatus"))
                            || StringUtils.isEmpty(petUserPro.getString("goodsInfo"))) {
                        //东东东东萌宠
                        log.info(jdCk.getPtPin() + "东东萌宠火爆1。。。");
                        jdPetEntity.setIsPetHei(1);
                        jdPetMapper.updateJdPet(jdPetEntity);
                        continue;
                    } else {
                        //互助码
                        String petShareCode = petUserPro.getString("shareCode");
                        jdCk.getJdPetEntity().setHelpCode(petShareCode);
                        jdPetMapper.updateJdPet(jdPetEntity);
                    }
                }
            }
            log.info("东东萌宠开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                try {
                    JdPetEntity toHelpJdPetEntity = toHelpJdCk.getJdPetEntity();
                    if (toHelpJdPetEntity.getToHelpStatus() != 1) {
                        continue;
                    }
                    if (jdPetEntity.getHelpStatus() == 1) {
                        //当前账号已满助力，跳过当前循环
                        log.info("{}东东萌宠已助力满!!!", jdCk.getRemark());
                        break;
                    }
                    if (jdCk.getCk().equals(toHelpJdCk.getCk())) {
                        //不能为自己助力
                        continue;
                    }
                    if (toHelpJdPetEntity.getIsPetHei() == 1) {
                        //东东萌宠火爆
                        continue;
                    }else {
                        JSONObject initPet = JdBeanChangeUtil.petRequest("initPetTown",toHelpJdCk.getCk());
                        helpWait("petShareHelp","东东萌宠" ,zlcwaittime);
                        if ("1018".equals(initPet.getString("resultCode")) || "1019".equals(initPet.getString("resultCode")) || "410".equals(initPet.getString("resultCode"))){
                            //东东萌宠火爆
                            log.info(toHelpJdCk.getPtPin() + "东东萌宠火爆2...");
                            toHelpJdPetEntity.setIsPetHei(1);
                            jdPetMapper.updateJdPet(toHelpJdPetEntity);
                            continue;
                        }
                    }
                    //助力
                    log.info("{}开始准备助力东东萌宠->{}", toHelpJdCk.getPtPin(), jdCk.getPtPin());
                    helpPet(toHelpJdCk, jdCk);
                    helpWait("petShareHelp","东东萌宠" ,zlcwaittime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("东东萌宠助力结束。。。");
    }

    @Async("asyncServiceExecutor")
    @Override
    public void plantShareHelp(List<JdCkEntity> cks, int zlcwaittime) {
        //判断是否有锁
        String fruitShareHelpIng = redis.get("plantShareHelp",false);
        if (StringUtils.isEmpty(fruitShareHelpIng)) {
            //加锁
            redis.put("plantShareHelp", "true", (zlcwaittime * 3L) * 1000);
        } else {
            //有锁
            log.info("种豆得豆互助进行中。。。");
            return;
        }

        //需要被助力的ck集合
        List<JdCkEntity> jdCks = new ArrayList<>();
        //查询所有的svipck 未助力满
        List<JdCkEntity> svipCks = new ArrayList<>();
        //查询所有的vipck 未助力满
        List<JdCkEntity> vipCks = new ArrayList<>();
        //查询所有普通用户的ck 未助力满
        List<JdCkEntity> ptCks = new ArrayList<>();
        //有助力的ck集合
        List<JdCkEntity> toHelpJdCks = new ArrayList<>();

        for (JdCkEntity ck : cks) {
            if (ck.getStatus() == 1) {
                //ck失效
                continue;
            }
            JdPlantEntity jdPlantEntity = ck.getJdPlantEntity();
            if (jdPlantEntity == null){
                jdPlantEntity = new JdPlantEntity();
                jdPlantEntity.setCkId(ck.getId());
                jdPlantEntity.setIsPlantHei(0);
                jdPlantEntity.setHelpStatus(0);
                jdPlantEntity.setToHelpStatus(1);
                jdPlantEntity.setTimes(0);
                ck.setJdPlantEntity(jdPlantEntity);
                jdPlantMapper.addJdPlant(jdPlantEntity);
            }
            if (jdPlantEntity.getIsPlantHei() == 0) {
                //种豆得豆不黑
                if ((ck.getLevel() == 0) && (jdPlantEntity.getHelpStatus() == 0)) {
                    //自己 && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdPlantEntity.getHelpStatus() == 0)) {
                    //vip && 未助力满 && 互助码不为空
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdPlantEntity.getHelpStatus() == 0)) {
                    //普通用户 && 未助力满 && 互助码不为空
                    ptCks.add(ck);
                }
                if (jdPlantEntity.getToHelpStatus() == 1) {
                    //还有助力
                    toHelpJdCks.add(ck);
                }
            }
        }

        jdCks.addAll(svipCks);
        jdCks.addAll(vipCks);
        Collections.shuffle(ptCks);
        jdCks.addAll(ptCks);

        Collections.shuffle(toHelpJdCks);

        log.info("查询到种豆得豆需要被助力的ck：{}个", jdCks.size());
        log.info("查询到种豆得豆可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("种豆得豆助力开始。。。");

        Integer limit = null;
        String zdddHelpStr = systemParamUtil.querySystemParam("ZDDDHELP");
        if (StringUtils.isNotEmpty(zdddHelpStr)) {
            limit = Integer.parseInt(zdddHelpStr);
            log.info("查询到种豆得豆助力上限：{}个", limit);
        }

        for (JdCkEntity jdCk : jdCks) {
            //过滤还有助力的
            toHelpJdCks = toHelpJdCks.stream().filter(ck -> (ck.getStatus() == 0) && (ck.getJdPlantEntity() == null || (ck.getJdPlantEntity().getIsPlantHei()) == 0 && ck.getJdPlantEntity().getToHelpStatus() == 1)).collect(Collectors.toList());
            int count = toHelpJdCks.size();
            if (count == 0){
                log.info("种豆得豆无可助力账号，结束");
                return;
            }
            log.info("种豆得豆可以去助力的还有：{}个", count);

            if (limit != null) {
                int plantTimes = jdPlantMapper.getHelpTimes();
                if (plantTimes >= limit) {
                    return;
                }
            }

            JdPlantEntity jdPlantEntity = jdCk.getJdPlantEntity();
            if (jdPlantEntity.getHelpStatus() == 1) {
                //当前账号已满助力，跳过当前循环
                log.info("{}种豆得豆已助力满!!!", jdCk.getRemark());
                continue;
            }
            if (jdPlantEntity.getIsPlantHei() == 1) {
                //种豆得豆火爆
                log.info(jdCk.getPtPin() + "种豆得豆火爆0。。。，跳过");
                continue;
            }
            if (StringUtils.isEmpty(jdPlantEntity.getHelpCode())){
                JSONObject plantInfo = getPlantInfo(jdCk.getCk(), "plantBeanIndex");
                helpWait("plantShareHelp","种豆得豆" ,zlcwaittime);
                if ("PB101".equals(plantInfo.getString("errorCode"))
                        || "PB003".equals(plantInfo.getString("errorCode"))
                        || "3".equals(plantInfo.getString("code"))) {
                    //种豆得豆火爆
                    log.info(jdCk.getPtPin() + "种豆得豆火爆1。。。，跳过");
                    jdPlantEntity.setIsPlantHei(1);
                    jdPlantMapper.updateJdPlant(jdPlantEntity);
                    continue;
                } else {
                    String shareUrl = plantInfo.getJSONObject("data").getJSONObject("jwordShareInfo").getString("shareUrl");
                    String plantShareCode = shareUrl.split("plantUuid=")[1];
                    jdPlantEntity.setHelpCode(plantShareCode);
                    jdPlantMapper.updateJdPlant(jdPlantEntity);
                }

            }

            log.info("种豆得豆开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                try {
                    JdPlantEntity toHelpJdPlantEntity = toHelpJdCk.getJdPlantEntity();
                    if (toHelpJdPlantEntity.getToHelpStatus() != 1) {
                        log.info(toHelpJdCk.getPtPin() + "已无可助力次数，跳过");
                        continue;
                    }
                    if (jdPlantEntity.getHelpStatus() == 1) {
                        //当前账号已满助力，跳过当前循环
                        log.info("{}种豆得豆已助力满，跳过", jdCk.getRemark());
                        break;
                    }
                    if (jdCk.getCk().equals(toHelpJdCk.getCk())) {
                        //不能为自己助力
                        continue;
                    }
                    if (toHelpJdPlantEntity.getIsPlantHei() == 1) {
                        //种豆得豆火爆
                        log.info(toHelpJdCk.getPtPin() + "种豆得豆火爆2。。。，跳过");
                        continue;
                    }else {
                        JSONObject plantInfo = getPlantInfo(toHelpJdCk.getCk(), "plantBeanIndex");
                        helpWait("plantShareHelp","种豆得豆" ,zlcwaittime);
                        if ("PB101".equals(plantInfo.getString("errorCode"))
                                || "PB003".equals(plantInfo.getString("errorCode"))
                                || "3".equals(plantInfo.getString("code"))){
                            //种豆得豆火爆
                            log.info(toHelpJdCk.getPtPin() + "种豆得豆火爆3。。。，跳过");
                            toHelpJdPlantEntity.setIsPlantHei(1);
                            jdPlantMapper.updateJdPlant(toHelpJdPlantEntity);
                            continue;
                        }
                    }
                    //助力
                    log.info("{}开始准备助力种豆得豆->{}", toHelpJdCk.getPtPin(), jdCk.getPtPin());
                    helpPlant(toHelpJdCk, jdCk);
                    helpWait("plantShareHelp","种豆得豆" ,zlcwaittime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("种豆得豆助力结束。。。");
    }

    /**
     * 查询所有ck
     */
    @Override
    public List<JdCkEntity> queryCksAndActivity() {
        return jdCkMapper.queryCksAndActivity();

    }

    /**
     * 获取助力信息
     */
    @Override
    public JSONObject getJdInfo() {
        JSONObject infos = new JSONObject();
        List<JSONObject> jdDatas = jdJdMapper.queryJd();
        //京豆收益
        TreeMap<String, String> jdIncome = new TreeMap<>();
        for (JSONObject jdData : jdDatas) {
            jdIncome.put(DateUtil.format(DateUtil.parse(jdData.getString("date")), "MM.dd"), jdData.getString("jdCount"));
        }
        infos.put("jdIncome", jdIncome);
        //ck总数
        Integer counts = jdCkMapper.queryCkCount();
        infos.put("allCkCount", counts);
        //东东农场
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryHasHelpFruitCk();
        infos.put("jdfruit", "已助力满==>" + jdCkEntities.size() + "个");
        //东东农场助力清单
        /*//农场抽奖
        int fruitHelpLottery = jdFruitMapper.getHelpLotteryTimes();
        infos.put("jdfruitLottery", "已助力满==>" + fruitHelpLottery + "个");*/
        //东东萌宠
        int petTimes = jdPetMapper.getHelpTimes();
        infos.put("jdpet", "已助力满==>" + petTimes + "个");
        //种豆得豆
        int plantTimes = jdPlantMapper.getHelpTimes();
        infos.put("jdbean", "已助力满==>" + plantTimes + "个");
        /*//东东工厂
        infos.put("jdddfactory", "待开发");
        //健康社区
        infos.put("jdhealth", "待开发");
        //京喜工厂
        infos.put("jdjxfactory", "待开发");
        //闪购盲盒
        infos.put("jdsgmh", "待开发");*/
        //京东登陆地址
        infos.put("jdLoginUrl", systemParamUtil.querySystemParam("JDLONGINURL"));
        return infos;
    }


    /**
     * 统计京豆收益
     */
    @Async("asyncServiceExecutor")
    @Override
    public void countJd() {
        log.info("统计京豆收益开始！！！");
        int pageSize = 100;
        //昨天
        DateTime date = DateUtil.offsetDay(DateUtil.date(), -1);
        Long begin = DateUtil.beginOfDay(date).getTime();
        Long end = DateUtil.endOfDay(date).getTime();
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryCksByLevels("0,1,2");
        log.info("共查询{}个ck", jdCkEntities.size());
        Collections.shuffle(jdCkEntities);
        boolean isBreak = false;
        for (JdCkEntity jdCkEntity : jdCkEntities) {
            if (isBreak){
                //ip已黑，下一个定时再跑
                break;
            }
            int page = 1;
            int today = 0;
            int yesterday = 0;
            int t = 0;
            do {
                log.info("统计" + URLDecoder.decode(jdCkEntity.getRemark(), CharsetUtil.defaultCharset()) + "京豆中。。。");
                JSONObject jingBeanBalanceDetail = null;
                try {
                    jingBeanBalanceDetail = getJingBeanBalanceDetail(jdCkEntity.getCk(), page, pageSize);
                } catch (Exception e) {
                    isBreak = true;
                }
                if (jingBeanBalanceDetail != null) {
                    if ("0".equals(jingBeanBalanceDetail.getString("code"))) {
                        page++;
                        List<JSONObject> detailList = jingBeanBalanceDetail.getJSONArray("detailList").toJavaList(JSONObject.class);
                        if (detailList.size()==0){
                            //跳出
                            t = 1;
                        }else {
                            for (JSONObject detail : detailList) {
                                Long time = DateUtil.parse(detail.getString("date")).getTime();
                        /*if (time >= end){
                            //今天

                        }*/
                                if (time <= end && time >= begin) {
                                    //昨天
                                    String eventMassage = detail.getString("eventMassage");
                                    if (!eventMassage.contains("退还") && !eventMassage.contains("物流") && !eventMassage.contains("扣赠")) {
                                        int amount = Integer.parseInt(detail.getString("amount"));
                                        if (amount > 0) {
                                            yesterday += amount;

                                        }
                                    }
                                }
                                if (time < begin) {
                                    //前天跳出
                                    t = 1;
                                }
                            }
                        }
                    } else if ("3".equals(jingBeanBalanceDetail.getString("code"))) {
                        log.info("ck已过期，或者填写不规范");
                        //跳出
                        t = 1;
                    } else {
                        log.info("未知情况：{}", jingBeanBalanceDetail.toJSONString());
                        t = 1;
                    }
                } else {
                    //跳出
                    t = 1;
                }
            } while (t == 0);
            log.info("统计" + jdCkEntity.getRemark() + "京豆结果：{}", yesterday);
            jdCkEntity.setJd(yesterday);
            jdCkEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
            jdCkMapper.updateCk(jdCkEntity);
        }
        log.info("统计京豆收益结束！！！");
        log.info("更新京豆数据！！！");
        int jdCount = jdCkMapper.sumJdCount();
        String dateTime = DateUtil.format(DateUtil.date(),"yyyy-MM-dd");
        int i = 0;
        if (jdJdMapper.queryJdByDate(dateTime) == null){
            i = jdJdMapper.addJdDate(dateTime, jdCount);
        }else {
            i = jdJdMapper.updateJdByDate(dateTime, jdCount);
        }
        if (i == 1) {
            log.info("京豆数据更新成功！！！");
        } else {
            log.info("京豆数据更新失败！！！");
        }
    }

    public JSONObject getJingBeanBalanceDetail(String ck, int page, int pageSize) {
        String result = HttpRequest.post("https://api.m.jd.com/client.action?functionId=getJingBeanBalanceDetail")
                .body("body=%7B%22pageSize%22%3A%22" + pageSize + "%22%2C%22page%22%3A%22" + page + "%22%7D&appid=ld")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("cookie", ck)
                .timeout(10000)
                .execute().body();
        log.info("查询京豆详情结果：{}", result);
        try {
            log.info("休息6s防止黑ip...");
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(result)) {
            log.info("查询京豆详情请求失败 ‼️‼️");
            return null;
        } else if (result.contains("response status: 403")) {
            throw new RuntimeException("接口返回403");
        } else {
            return JSONObject.parseObject(result);
        }
    }

    /**
     * 青龙同步助力池
     */
    @Override
    public void qlToZlc() {
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (QlEntity ql : qlEntities) {
            log.info("青龙服务器：{}开始同步助力池。。。", ql.getRemark());
            int count = 0;
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            for (QlEnv env : envs) {
                try {
                    String name = env.getName();
                    String ck = env.getValue();
                    String remarks = env.getRemarks();
                    Integer status = env.getStatus();
                    if ("JD_COOKIE".equals(name)) {
                        addJdck(ck,remarks,status,count,ql.getRemark());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("青龙服务器：{}同步助力池结束，共同步{}个ck", ql.getRemark(), count);
        }
    }

    /**
     *
     * @param ck ck
     * @param remarks ck备注
     * @param count 数量
     * @param qlRemark 青龙备注
     */
    @Override
    public void addJdck(String ck, String remarks,Integer status, int count, String qlRemark){
        String ptPin = "";
        Matcher matcher = jdPinPattern.matcher(ck);
        if (matcher.find()) {
            ptPin = matcher.group(1);
        }
        //判断pt_pin是否存在
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        jdCkEntityQuery.setPtPin(ptPin);
        JdCkEntity jdck = jdCkMapper.queryCk(jdCkEntityQuery);
        if (jdck == null) {
            //添加
            jdck = new JdCkEntity();
            jdck.setStatus(status);
            jdck.setCk(ck);
            jdck.setQlRemark(qlRemark);
            jdck.setLevel(2);
            if (StringUtils.isNotEmpty(remarks)) {
                jdck.setRemark(remarks);
            }
            jdck.setPtPin(ptPin);
            jdCkMapper.addCk(jdck);
            count++;
        } else {
            //更新
            if (!ck.equals(jdck.getCk()) || (StringUtils.isNotEmpty(qlRemark) && !qlRemark.equals(jdck.getQlRemark())) || !status.equals(jdck.getStatus())) {
                jdck.setCk(ck);
                jdck.setQlRemark(qlRemark);
                jdck.setStatus(status);
                if (StringUtils.isNotEmpty(remarks)) {
                    jdck.setRemark(remarks);
                }
                jdck.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                jdCkMapper.updateCk(jdck);
                count++;
            }
        }
    }

    @Override
    public List getHelpList(String type) {
        ArrayList<String> list = new ArrayList<>();
        List<JdCkEntity> jdCkEntities = new ArrayList<>();
        switch (type) {
            case "fruit":
                jdCkEntities = jdCkMapper.queryHasHelpFruitCk();
                break;
            case "pet":
                jdCkEntities = jdCkMapper.queryHasHelpPetCk();
                break;
            case "plant":
                jdCkEntities = jdCkMapper.queryHasHelpPlantCk();
                break;
        }
        for (JdCkEntity jdCkEntity : jdCkEntities) {
            String name = "";
            String remark = jdCkEntity.getRemark();
            if (StringUtils.isNotEmpty(remark)) {
                if (remark.contains("@@")) {
                    int index = remark.indexOf("@@");
                    name = jdCkEntity.getRemark().substring(0, index);
                } else {
                    name = jdCkEntity.getRemark();
                }
            } else {
                name = jdCkEntity.getPtPin();
            }
            list.add(name);
        }
        return list;
    }

    @Override
    public void clear() {
        jdFruitMapper.clear();
        jdPetMapper.clear();
        jdPlantMapper.clear();
        jdZqdyjMapper.clear();
    }

    /**
     * 获取助力池信息
     * @return
     */
    @Override
    public JSONObject getZlcInfo() {
        List<JdFruitEntity> JdFruits = jdFruitMapper.getJdFruits();
        //农场火爆数
        long fruitHot = JdFruits.stream().filter(f -> f != null && f.getIsFruitHei() == 1).count();
        //农场已助力个数
        long fruitHasHelp = JdFruits.stream().filter(f -> f != null && f.getHelpStatus() == 1).count();
        //农场有助力数
        long fruitHaveHelp = JdFruits.stream().filter(f -> f == null || (f.getToHelpStatus() == 1 && f.getIsFruitHei() == 0)).count();

        List<JdPetEntity> jdPets = jdPetMapper.getJdPets();
        //萌宠火爆数
        long petHot = jdPets.stream().filter(p -> p != null && p.getIsPetHei() == 1).count();
        //萌宠已助力个数
        long petHasHelp = jdPets.stream().filter(p -> p != null && p.getHelpStatus() == 1).count();
        //萌宠有助力数
        long petHaveHelp = jdPets.stream().filter(p -> p == null || (p.getToHelpStatus() == 1 && p.getIsPetHei() == 0)).count();

        List<JdPlantEntity> jdPlants = jdPlantMapper.getJdPlants();
        //种豆火爆数
        long plantHot = jdPlants.stream().filter(p -> p != null && p.getIsPlantHei() == 1).count();
        //种豆已助力个数
        long plantHasHelp = jdPlants.stream().filter(p -> p != null && p.getHelpStatus() == 1).count();
        //种豆有助力数
        long plantHaveHelp = jdPlants.stream().filter(p -> p == null || (p.getToHelpStatus() == 1 && p.getIsPlantHei() == 0)).count();

        JSONObject data = new JSONObject();
        JSONObject fruit = new JSONObject();
        fruit.put("fruitHot",fruitHot);
        fruit.put("fruitHasHelp",fruitHasHelp);
        fruit.put("fruitHaveHelp",fruitHaveHelp);
        String fruitShareHelp = redis.get("fruitShareHelp",false);
        if (StringUtils.isEmpty(fruitShareHelp)) {
            fruit.put("run",false);
        }else {
            fruit.put("run",true);
        }
        data.put("fruit",fruit);
        JSONObject pet = new JSONObject();
        pet.put("petHot",petHot);
        pet.put("petHasHelp",petHasHelp);
        pet.put("petHaveHelp",petHaveHelp);
        String petShareHelp = redis.get("petShareHelp",false);
        if (StringUtils.isEmpty(petShareHelp)) {
            pet.put("run",false);
        }else {
            pet.put("run",true);
        }
        data.put("pet",pet);
        JSONObject plant = new JSONObject();
        plant.put("plantHot",plantHot);
        plant.put("plantHasHelp",plantHasHelp);
        plant.put("plantHaveHelp",plantHaveHelp);
        String plantShareHelp = redis.get("plantShareHelp",false);
        if (StringUtils.isEmpty(plantShareHelp)) {
            plant.put("run",false);
        }else {
            plant.put("run",true);
        }
        data.put("plant",plant);
        return data;
    }

    @Override
    public void resetHot(Integer type) {
        switch (type){
            case 0:
                //东东农场
                jdFruitMapper.resetHot();
                break;
            case 1:
                //东东萌宠
                jdPetMapper.resetHot();
                break;
            case 2:
                //种豆得豆
                jdPlantMapper.resetHot();
                break;
            default:

        }
    }

    /**
     * 东东农场助力
     *
     * @param toHelpJdCk 助力的ck
     * @param jdCk       被助力的ck
     * @return
     */
    public void helpFruit(JdCkEntity toHelpJdCk, JdCkEntity jdCk) {
        JdFruitEntity jdFruitEntity = jdCk.getJdFruitEntity();
        JdFruitEntity toHelpJdFruitEntity = toHelpJdCk.getJdFruitEntity();
        log.info("{}开始助力东东农场->{}", toHelpJdCk.getPtPin(), jdCk.getPtPin());
        JSONObject body = new JSONObject();
        body.put("imageUrl", "");
        body.put("nickName", "");
        //被助力的助力码
        body.put("shareCode", jdFruitEntity.getHelpCode());
        body.put("babelChannel", "3");
        body.put("version", 2);
        body.put("channel", 1);
        URLEncoder urlEncoder = URLEncoder.createDefault();
        String result = HttpRequest.get(JDAPIHOST + "?functionId=initForFarm&body=" + urlEncoder.encode(body.toJSONString(), Charset.defaultCharset()) + "&appid=wh5")
                .header("Host", "api.m.jd.com")
                .header("Accept", "*/*")
                .header("Origin", "https://carry.m.jd.com")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-CN,zh-Hans;q=0.9")
                .header("Referer", "https://carry.m.jd.com/")
                .header("Cookie", toHelpJdCk.getCk())
                .timeout(10000)
                .execute().body();
        log.info("东东农场助力结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("东东农场: API查询请求失败 ‼️‼️");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            String msg = "【东东农场助力好友结果】:"+toHelpJdCk.getPtPin()+"->"+jdCk.getPtPin();
            if ("0".equals(resultObject.getString("code"))) {
                JSONObject helpResult = resultObject.getJSONObject("helpResult");
                if ("0".equals(helpResult.getString("code"))) {
                    //助力成功
                    log.info(msg + " 已成功助力");
                } else if ("8".equals(helpResult.getString("code"))) {
                    log.info(msg + " 助力失败，您今天助力次数已耗尽");
                    toHelpJdFruitEntity.setToHelpStatus(0);
                    jdFruitMapper.updateJdFruit(toHelpJdFruitEntity);
                } else if ("9".equals(helpResult.getString("code"))) {
                    log.info(msg + " 之前助力过了");
                } else if ("10".equals(helpResult.getString("code"))) {
                    log.info(msg + " 好友已满助力");
                    jdFruitEntity.setHelpStatus(1);
                    jdFruitMapper.updateJdFruit(jdFruitEntity);
                } else {
                    log.info("东东农场助力其他情况：{}", helpResult.toJSONString());
                }
                String remainTimes = helpResult.getString("remainTimes");
                log.info("【东东农场今日助力次数还剩】{}次", remainTimes);
                if ("0".equals(remainTimes)) {
                    log.info("东东农场当前助力次数已耗尽");
                    toHelpJdFruitEntity.setToHelpStatus(0);
                    jdFruitMapper.updateJdFruit(toHelpJdFruitEntity);
                }
            } else if ("3".equals(resultObject.getString("code"))) {
                log.info("ck已过期 ‼️‼️");
                toHelpJdCk.setStatus(1);
                toHelpJdCk.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                jdCkMapper.updateCk(toHelpJdCk);
            } else {
                log.info("东东农场助力失败 ‼️‼️");
            }
            /*Integer times = toHelpJdFruitEntity.getTimes();
            times++;
            if (times > 10) {
                toHelpJdFruitEntity.setToHelpStatus(0);
            } else {
                toHelpJdFruitEntity.setTimes(times);
            }*/
        }
    }

    /**
     * 东东农场天天抽奖助力
     *
     * @param toHelpJdCk 助力的ck
     * @param jdCk       被助力的ck
     * @return
     */
    public void helpFruitLottery(JdCkEntity toHelpJdCk, JdCkEntity jdCk) {
        JdFruitEntity jdFruitEntity = jdCk.getJdFruitEntity();
        JdFruitEntity toHelpJdFruitEntity = toHelpJdCk.getJdFruitEntity();
        log.info("{}开始助力东东农场天天抽奖->{}", toHelpJdFruitEntity.getHelpCode(), jdFruitEntity.getHelpCode());
        JSONObject body = new JSONObject();
        body.put("imageUrl", "");
        body.put("nickName", "");
        //被助力的助力码
        body.put("shareCode", jdFruitEntity.getHelpCode() + "-3");
        body.put("babelChannel", "3");
        body.put("version", 4);
        body.put("channel", 1);
        String result = get("initForFarm", body.toJSONString(), toHelpJdCk.getCk());
        log.info("东东农场天天抽奖助力结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("东东农场天天抽奖: API查询请求失败 ‼️‼️");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            if ("0".equals(resultObject.getString("code"))) {
                JSONObject helpResult = resultObject.getJSONObject("helpResult");
                if ("0".equals(helpResult.getString("code"))) {
                    //助力成功
                    log.info("【东东农场天天抽奖助力好友结果】: 已成功助力");
                } else if ("13".equals(helpResult.getString("code"))) {
                    log.info("【东东农场天天抽奖助力好友结果】: 助力失败，您今天助力次数已耗尽");
                    JdFruitEntity update = new JdFruitEntity();
                    update.setId(toHelpJdFruitEntity.getId());
                    update.setToHelpLotteryStatus(0);
                    jdFruitMapper.updateJdFruit(update);
                } else if ("11".equals(helpResult.getString("code"))) {
                    log.info("【东东农场天天抽奖助力好友结果】: 之前助力过了");
                } else if ("10".equals(helpResult.getString("code"))) {
                    log.info("【东东农场天天抽奖助力好友结果】: 好友已满助力");
                    JdFruitEntity update = new JdFruitEntity();
                    update.setId(jdFruitEntity.getId());
                    update.setHelpLotteryStatus(1);
                    jdFruitMapper.updateJdFruit(update);
                } else {
                    log.info("东东农场天天抽奖助力其他情况：{}", helpResult.toJSONString());
                }
            } else if ("400".equals(resultObject.getString("code"))) {
                log.info("东东农场天天抽奖火爆 ‼️‼️");
                JdFruitEntity update = new JdFruitEntity();
                update.setIsFruitHei(1);
//                jdFruitMapper.updateJdFruit(update);
            } else if ("3".equals(resultObject.getString("code"))) {
                log.info("ck已过期 ‼️‼️");
                toHelpJdCk.setStatus(1);
                toHelpJdCk.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                jdCkMapper.updateCk(toHelpJdCk);
            } else {
                log.info("东东农场天天抽奖助力失败 ‼️‼️");
            }
        }
    }

    /**
     * 东东萌宠助力
     *
     * @param toHelpJdCk 助力的ck
     * @param jdCk       被助力的ck
     * @return
     */
    public void helpPet(JdCkEntity toHelpJdCk, JdCkEntity jdCk) {
        JdPetEntity jdPetEntity = jdCk.getJdPetEntity();
        JdPetEntity toHelpJdPetEntity = toHelpJdCk.getJdPetEntity();
        log.info("{}开始助力东东萌宠->{}", toHelpJdCk.getPtPin(), jdCk.getPtPin());
        JSONObject body = new JSONObject();
        body.put("shareCode", jdPetEntity.getHelpCode());
        body.put("version", 2);
        body.put("channel", "app");
        String result = HttpRequest.post(JDAPIHOST + "?functionId=slaveHelp")
                .body("body=" + EscapeUtil.escape(body.toJSONString()) + "&appid=wh5&loginWQBiz=pet-town&clientVersion=9.0.4")
                .header("cookie", toHelpJdCk.getCk())
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(10000)
                .execute().body();
        log.info("东东萌宠助力结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("东东萌宠: API查询请求失败 ‼️‼️");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            String msg = "【东东萌宠助力好友结果】:"+toHelpJdCk.getPtPin()+"->"+jdCk.getPtPin();
            if ("0".equals(resultObject.getString("code")) && "0".equals(resultObject.getString("resultCode"))) {
                JSONObject helpResult = resultObject.getJSONObject("result");
                if ("0".equals(helpResult.getString("helpStatus"))) {
                    //助力成功
                    log.info(msg + " 已成功助力");
                } else if ("1".equals(helpResult.getString("helpStatus"))) {
                    log.info(msg + " 助力失败，您今天助力次数已耗尽");
                    toHelpJdPetEntity.setToHelpStatus(0);
                    jdPetMapper.updateJdPet(toHelpJdPetEntity);
                } else if ("2".equals(helpResult.getString("helpStatus"))) {
                    log.info(msg + " 好友已满助力");
                    jdPetEntity.setHelpStatus(1);
                    jdPetMapper.updateJdPet(jdPetEntity);
                } else {
                    log.info("东东萌宠助力其他情况：{}", helpResult.toJSONString());
                }
            } else if ("1002".equals(resultObject.getString("resultCode"))) {
                log.info(resultObject.getString("message"));
                toHelpJdPetEntity.setIsPetHei(1);
                jdPetMapper.updateJdPet(toHelpJdPetEntity);
            }else if ("已经助过力".equals(resultObject.getString("message"))) {
                log.info("此账号今天已经跑过助力了，跳出....");
            } else {
                log.info("东东萌宠助力失败:{}", resultObject.toJSONString());
            }
            /*Integer times = toHelpJdPetEntity.getTimes();
            times++;
            if (times > 10) {
                toHelpJdPetEntity.setToHelpStatus(0);
            } else {
                toHelpJdPetEntity.setTimes(times);
            }*/
        }
    }

    /**
     * 种豆得豆助力
     *
     * @param toHelpJdCk 助力的ck
     * @param jdCk       被助力的ck
     * @return
     */
    public void helpPlant(JdCkEntity toHelpJdCk, JdCkEntity jdCk) {
        JdPlantEntity jdPlantEntity = jdCk.getJdPlantEntity();
        JdPlantEntity toHelpJdPlantEntity = toHelpJdCk.getJdPlantEntity();
        log.info("{}开始助力种豆得豆->{}", toHelpJdCk.getPtPin(), jdCk.getPtPin());
        JSONObject body = new JSONObject();
        body.put("plantUuid", jdPlantEntity.getHelpCode());
        body.put("wxHeadImgUrl", "");
        body.put("shareUuid", "");
        body.put("followType", "1");
        String result = HttpRequest.post(JDAPIHOST)
                .body("functionId=plantBeanIndex&body=" + URLEncodeUtil.encode(body.toJSONString()) + "&appid=ld&client=apple&area=19_1601_50258_51885&build=167490&clientVersion=9.3.2")
                .header("cookie", toHelpJdCk.getCk())
                .header("Accept", "*/*")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-Hans-CN;q=1,en-CN;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(10000)
                .execute().body();
        log.info("种豆得豆助力结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("种豆得豆: API查询请求失败 ‼️‼️");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            String msg = "【种豆得豆助力好友结果】:"+toHelpJdCk.getPtPin()+"->"+jdCk.getPtPin();
            if ("0".equals(resultObject.getString("code")) && StringUtils.isNotEmpty(resultObject.getString("data"))) {
                JSONObject helpResult = resultObject.getJSONObject("data");
                if ("1".equals(helpResult.getJSONObject("helpShareRes").getString("state"))) {
                    //助力成功
                    log.info(msg + " 已成功助力");
                } else if ("2".equals(helpResult.getJSONObject("helpShareRes").getString("state"))) {
                    log.info(msg + "助力失败，您今天助力次数已耗尽");
                    toHelpJdPlantEntity.setToHelpStatus(0);
                    jdPlantMapper.updateJdPlant(toHelpJdPlantEntity);
                } else if ("3".equals(helpResult.getJSONObject("helpShareRes").getString("state"))) {
                    log.info(msg + "好友已满助力");
                    jdPlantEntity.setHelpStatus(1);
                    jdPlantMapper.updateJdPlant(jdPlantEntity);
                } else {
                    log.info("种豆得豆助力其他情况：{}", helpResult.toJSONString());
                }
            } else {
                log.info("种豆得豆助力失败:{}", resultObject.toJSONString());
            }
            /*Integer times = toHelpJdPlantEntity.getTimes();
            times++;
            if (times > 10) {
                toHelpJdPlantEntity.setToHelpStatus(0);
            } else {
                toHelpJdPlantEntity.setTimes(times);
            }*/
        }
    }

    /**
     * 根据ck查询农场信息
     *
     * @param ck
     * @return
     */
    public JSONObject getFarmInfo(String ck) {
        JSONObject version = new JSONObject();
        version.put("version", 14);
        String result = HttpRequest.post(JDAPIHOST + "?functionId=initForFarm")
                .body("body=%7B%22version%22%3A14%7D&appid=wh5&clientVersion=9.1.0")
                .header("accept", "*/*")
                .header("accept-encoding", "gzip, deflate, br")
                .header("accept-language", "zh-CN,zh;q=0.9")
                .header("cache-control", "no-cache")
                .header("cookie", ck)
                .header("origin", "https://home.m.jd.com")
                .header("pragma", "no-cache")
                .header("referer", "https://home.m.jd.com/myJd/newhome.action")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-site")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(10000)
                .execute().body();
        log.info("查询农场信息结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("东东农场: 查询农场信息请求失败 ‼️‼️");
            return null;
        } else {
            return JSONObject.parseObject(result);
        }
    }


    /**
     * 根据ck查询萌宠信息
     *
     * @param ck
     * @return
     */
    public JSONObject getPetInfo(String ck, String functionId) {
        JSONObject body = new JSONObject();
        body.put("version", 2);
        body.put("channel", "app");
        String result = HttpRequest.post(JDAPIHOST + "?functionId=" + functionId)
                .body("body=" + EscapeUtil.escape(body.toJSONString()) + "&appid=wh5&loginWQBiz=pet-town&clientVersion=9.0.4")
                .header("cookie", ck)
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(10000)
                .execute().body();
        log.info("查询萌宠信息结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("东东萌宠: 查询萌宠信息请求失败 ‼️‼️");
            return null;
        } else {
            return JSONObject.parseObject(result);
        }
    }

    /**
     * 根据ck查询种豆得豆信息
     *
     * @param ck
     * @return
     */
    public JSONObject getPlantInfo(String ck, String functionId) {
        JSONObject body = new JSONObject();
        body.put("version", "9.2.4.1");
        body.put("monitor_source", "plant_app_plant_index");
        body.put("monitor_refer", "");
        String result = HttpRequest.post(JDAPIHOST)
                .body("functionId=" + functionId + "&body=" + URLEncodeUtil.encode(body.toJSONString()) + "&appid=ld&client=apple&area=19_1601_50258_51885&build=167490&clientVersion=9.3.2")
                .header("cookie", ck)
                .header("Accept", "*/*")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-Hans-CN;q=1,en-CN;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(10000)
                .execute().body();
        log.info("查询种豆得豆信息结果：{}", result);
        try {
            log.info("休息1s防止黑ip...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(result)) {
            log.info("种豆得豆: 查询种豆得豆信息请求失败 ‼️‼️");
            return null;
        } else {
            return JSONObject.parseObject(result);
        }
    }

    /**
     * 初始化集卡抽奖活动数据API
     *
     * @return
     */
    public boolean initForTurntableFarm(String cookie) {
        JSONObject body = new JSONObject();
        body.put("version", 4);
        body.put("channel", 1);
        String functionId = "initForTurntableFarm";
        String resStr = get(functionId, body.toJSONString(), cookie);
        log.info("初始化天天抽奖得好礼返回：{}", resStr);
        if (StringUtils.isEmpty(resStr)) {
            log.info("初始化天天抽奖得好礼失败");
            return false;
        } else {
            JSONObject res = JSONObject.parseObject(resStr);
            if ("0".equals(res.getString("code"))) {
                return true;
            } else {
                log.info("初始化天天抽奖得好礼失败");
                return false;
            }
        }
    }

    public String get(String functionId, String body, String cookie) {
        URLEncoder urlEncoder = URLEncoder.createDefault();
        String res = HttpRequest.get(JDAPIHOST + "?functionId=" + functionId + "&body=" + urlEncoder.encode(body, Charset.defaultCharset()) + "&appid=wh5")
                .header("Host", "api.m.jd.com")
                .header("Accept", "*/*")
                .header("Origin", "https://carry.m.jd.com")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-CN,zh-Hans;q=0.9")
                .header("Referer", "https://carry.m.jd.com/")
                .header("Cookie", cookie)
                .execute().body();
        try {
            log.info("休息20s防止黑ip...");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

}
