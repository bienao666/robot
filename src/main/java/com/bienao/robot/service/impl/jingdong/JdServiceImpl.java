package com.bienao.robot.service.impl.jingdong;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.entity.jingdong.JdFruitEntity;
import com.bienao.robot.entity.jingdong.JdPetEntity;
import com.bienao.robot.entity.jingdong.JdPlantEntity;
import com.bienao.robot.mapper.jingdong.*;
import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.service.jingdong.JdService;
import com.bienao.robot.utils.jingdong.CommonUtil;
import com.bienao.robot.utils.jingdong.GetUserAgentUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;

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
    private CkService ckService;

    @Autowired
    private SystemParamUtil systemParamUtil;

    /**
     * 东东农场互助
     */
    @Async("asyncServiceExecutor")
    @Override
    public void fruitShareHelp(List<JdCkEntity> cks, int zlcwaittime) {
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
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdFruitEntity jdFruitEntity = ck.getJdFruitEntity();
            if (jdFruitEntity!= null && (jdFruitEntity.getIsFruitHei() == 0)) {
                //东东农场不黑
                if ((ck.getLevel() == 0) && (jdFruitEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //自己 && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdFruitEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //svip && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdFruitEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //vip && 未助力满 && 互助码不为空
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 3) && (jdFruitEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
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

        //排序
        jdCks = CommonUtil.getHelpCks(jdCks, vipCks, ptCks);

        Collections.shuffle(toHelpJdCks);

        Integer limit = null;
        String ddncHelpStr = systemParamUtil.querySystemParam("DDNCHELP");
        if (StringUtils.isNotEmpty(ddncHelpStr)){
            limit = Integer.parseInt(ddncHelpStr);
            log.info("查询到东东农场助力上限：{}个", limit);
        }
        log.info("查询到东东农场需要被助力的ck：{}个", jdCks.size());
        log.info("查询到东东农场可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("东东农场助力开始。。。");

        for (JdCkEntity jdCk : jdCks) {
            JdFruitEntity jdFruitEntity = jdCk.getJdFruitEntity();
            log.info("东东农场开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                JdFruitEntity toHelpJdFruitEntity = toHelpJdCk.getJdFruitEntity();
                if (jdFruitEntity.getHelpStatus() == 1) {
                    //当前账号已满助力，跳过当前循环
                    log.info("{}东东农场已助力满!!!", jdCk.getRemark());
                    break;
                }
                if (jdFruitEntity.getHelpCode().equals(toHelpJdFruitEntity.getHelpCode())) {
                    //不能为自己助力
                    continue;
                }
                if (StringUtils.isEmpty(toHelpJdFruitEntity.getHelpCode())){
                    //活动未初始化
                    continue;
                }
                if (toHelpJdFruitEntity.getIsFruitHei() == 1) {
                    //东东农场火爆
                    continue;
                }
                if (toHelpJdFruitEntity.getToHelpStatus() == 1) {
                    //助力
                    helpFruit(toHelpJdCk, jdCk);
                    try {
                        log.info("东东农场助力休息"+zlcwaittime*1000+"s防止黑ip...");
                        Thread.sleep(zlcwaittime*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (limit!=null){
                int fruitHelp = jdFruitMapper.getHelpTimes();
                if (fruitHelp>=limit){
                    return;
                }
            }
        }
        log.info("东东农场助力结束。。。");
    }

    /**
     * 东东农场天天抽奖互助
     */
    @Async("asyncServiceExecutor")
    @Override
    public void fruitLotteryShareHelp(List<JdCkEntity> cks, int zlcwaittime) {
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
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdFruitEntity jdFruitEntity = ck.getJdFruitEntity();
            if (jdFruitEntity!= null && (jdFruitEntity.getIsFruitHei() == 0)) {
                //东东农场不黑
                if ((ck.getLevel() == 0) && (jdFruitEntity.getHelpLotteryStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //自己 && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdFruitEntity.getHelpLotteryStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //svip && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdFruitEntity.getHelpLotteryStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //vip && 未助力满 && 互助码不为空
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 3) && (jdFruitEntity.getHelpLotteryStatus() == 0) && (StringUtils.isNotEmpty(jdFruitEntity.getHelpCode()))) {
                    //普通用户 && 未助力满 && 互助码不为空
                    ptCks.add(ck);
                }
                if (jdFruitEntity.getToHelpLotteryStatus() == 1) {
                    //还有助力
                    toHelpJdCks.add(ck);
                }
            }
        }

        jdCks.addAll(svipCks);

        //随机排序
        Collections.shuffle(vipCks);
        List<JdCkEntity> vipCks1 = vipCks.subList(0,vipCks.size() / 10);
        List<JdCkEntity> vipCks2 = vipCks.subList(vipCks.size() / 10,vipCks.size());
        //添加vipCk第一部分
        jdCks.addAll(vipCks1);
        //随机排序
        Collections.shuffle(ptCks);
        List<JdCkEntity> ptCks1 = ptCks.subList(0,ptCks.size() / 10);
        List<JdCkEntity> ptCks2 = ptCks.subList(ptCks.size() / 10,ptCks.size());
        //添加ck第一部分
        jdCks.addAll(ptCks1);

        //添加剩下的部分
        jdCks.addAll(vipCks2);
        jdCks.addAll(ptCks2);

        Collections.shuffle(toHelpJdCks);

        log.info("查询到需要被助力的ck：{}个", jdCks.size());
        log.info("查询到可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("东东农场天天抽奖助力开始。。。");

        for (JdCkEntity jdCk : jdCks) {
            JdFruitEntity jdFruitEntity = jdCk.getJdFruitEntity();
            log.info("东东农场天天抽奖开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                JdFruitEntity toHelpJdFruitEntity = toHelpJdCk.getJdFruitEntity();
                if (jdFruitEntity.getHelpLotteryStatus() == 1) {
                    //当前账号已满助力，跳过当前循环
                    log.info("{}东东农场天天抽奖已助力满!!!", jdCk.getRemark());
                    break;
                }
                if (jdFruitEntity.getHelpCode().equals(toHelpJdFruitEntity.getHelpCode())) {
                    //不能为自己助力
                    continue;
                }
                if (StringUtils.isEmpty(toHelpJdFruitEntity.getHelpCode())){
                    //活动未初始化
                    continue;
                }
                if (toHelpJdFruitEntity.getIsFruitHei() == 1) {
                    //东东农场火爆
                    continue;
                }
                if (toHelpJdFruitEntity.getToHelpLotteryStatus() == 1) {
                    //助力
                    helpFruitLottery(toHelpJdCk, jdCk);
                    log.info("东东农场天天抽奖助力休息"+zlcwaittime*1000+"s防止黑ip...");
                    try {
                        Thread.sleep(zlcwaittime*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        log.info("东东农场天天抽奖助力结束。。。");
    }

    /**
     * 东东萌宠互助
     */
    @Async("asyncServiceExecutor")
    @Override
    public void petShareHelp(List<JdCkEntity> cks, int zlcwaittime) {
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
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdPetEntity jdPetEntity = ck.getJdPetEntity();
            if (jdPetEntity!= null && (jdPetEntity.getIsPetHei() == 0)) {
                //东东萌宠不黑
                if ((ck.getLevel() == 0) && (jdPetEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPetEntity.getHelpCode()))) {
                    //自己 && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdPetEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPetEntity.getHelpCode()))) {
                    //svip && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdPetEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPetEntity.getHelpCode()))) {
                    //vip && 未助力满 && 互助码不为空
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 3) && (jdPetEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPetEntity.getHelpCode()))) {
                    //普通用户 && 未助力满 && 互助码不为空
                    ptCks.add(ck);
                }
                if (jdPetEntity.getToHelpStatus() == 1) {
                    //还有助力
                    toHelpJdCks.add(ck);
                }
            }
        }

        jdCks.addAll(svipCks);

        //排序
        jdCks = CommonUtil.getHelpCks(jdCks, vipCks, ptCks);

        Collections.shuffle(toHelpJdCks);

        Integer limit = null;
        String ddmcHelpStr = systemParamUtil.querySystemParam("DDMCHELP");
        if (StringUtils.isNotEmpty(ddmcHelpStr)){
            limit = Integer.parseInt(ddmcHelpStr);
            log.info("查询到东东萌宠助力上限：{}个", limit);
        }

        log.info("查询到东东萌宠需要被助力的ck：{}个", jdCks.size());
        log.info("查询到东东萌宠可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("东东萌宠助力开始。。。");

        for (JdCkEntity jdCk : jdCks) {
            JdPetEntity jdPetEntity = jdCk.getJdPetEntity();
            log.info("东东萌宠开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                JdPetEntity toHelpJdPetEntity = toHelpJdCk.getJdPetEntity();
                if (jdPetEntity.getHelpStatus() == 1) {
                    //当前账号已满助力，跳过当前循环
                    log.info("{}东东萌宠已助力满!!!", jdCk.getRemark());
                    break;
                }
                if (jdPetEntity.getHelpCode().equals(toHelpJdPetEntity.getHelpCode())) {
                    //不能为自己助力
                    continue;
                }
                if (StringUtils.isEmpty(toHelpJdPetEntity.getHelpCode())){
                    //活动未初始化
                    continue;
                }
                if (toHelpJdPetEntity.getIsPetHei() == 1) {
                    //东东农场火爆
                    continue;
                }
                if (toHelpJdPetEntity.getToHelpStatus() == 1) {
                    //助力
                    helpPet(toHelpJdCk, jdCk);
                    try {
                        log.info("东东萌宠助力休息"+zlcwaittime*1000+"s防止黑ip...");
                        Thread.sleep(zlcwaittime*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (limit!=null){
                int petTimes = jdPetMapper.getHelpTimes();
                if (petTimes>=limit){
                    return;
                }
            }
        }
        log.info("东东萌宠助力结束。。。");
    }

    @Async("asyncServiceExecutor")
    @Override
    public void plantShareHelp(List<JdCkEntity> cks,int zlcwaittime) {
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
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdPlantEntity jdPlantEntity = ck.getJdPlantEntity();
            if (jdPlantEntity!= null && (jdPlantEntity.getIsPlantHei() == 0)) {
                //种豆得豆不黑
                if ((ck.getLevel() == 0) && (jdPlantEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPlantEntity.getHelpCode()))) {
                    //自己 && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 1) && (jdPlantEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPlantEntity.getHelpCode()))) {
                    //svip && 未助力满 && 互助码不为空
                    svipCks.add(ck);
                }
                if ((ck.getLevel() == 2) && (jdPlantEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPlantEntity.getHelpCode()))) {
                    //vip && 未助力满 && 互助码不为空
                    vipCks.add(ck);
                }
                if ((ck.getLevel() == 3) && (jdPlantEntity.getHelpStatus() == 0) && (StringUtils.isNotEmpty(jdPlantEntity.getHelpCode()))) {
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

        //排序
        jdCks = CommonUtil.getHelpCks(jdCks, vipCks, ptCks);

        Collections.shuffle(toHelpJdCks);

        Integer limit = null;
        String zdddHelpStr = systemParamUtil.querySystemParam("ZDDDHELP");
        if (StringUtils.isNotEmpty(zdddHelpStr)){
            limit = Integer.parseInt(zdddHelpStr);
            log.info("查询到种豆得豆助力上限：{}个", limit);
        }

        log.info("查询到种豆得豆需要被助力的ck：{}个", jdCks.size());
        log.info("查询到种豆得豆可以去助力的ck：{}个", toHelpJdCks.size());
        log.info("种豆得豆助力开始。。。");

        for (JdCkEntity jdCk : jdCks) {
            JdPlantEntity jdPlantEntity = jdCk.getJdPlantEntity();
            log.info("种豆得豆开始助力{}!!!", jdCk.getRemark());
            for (JdCkEntity toHelpJdCk : toHelpJdCks) {
                JdPlantEntity toHelpJdPlantEntity = toHelpJdCk.getJdPlantEntity();
                if (jdPlantEntity.getHelpStatus() == 1) {
                    //当前账号已满助力，跳过当前循环
                    log.info("{}种豆得豆已助力满!!!", jdCk.getRemark());
                    break;
                }
                if (jdPlantEntity.getHelpCode().equals(toHelpJdPlantEntity.getHelpCode())) {
                    //不能为自己助力
                    continue;
                }
                if (StringUtils.isEmpty(toHelpJdPlantEntity.getHelpCode())){
                    //活动未初始化
                    continue;
                }
                if (toHelpJdPlantEntity.getIsPlantHei() == 1) {
                    //种豆得豆火爆
                    continue;
                }
                if (toHelpJdPlantEntity.getToHelpStatus() == 1) {
                    //助力
                    helpPlant(toHelpJdCk, jdCk);
                    try {
                        log.info("种豆得豆助力休息"+zlcwaittime*1000+"s防止黑ip...");
                        Thread.sleep(zlcwaittime*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (limit!=null){
                int plantTimes = jdPlantMapper.getHelpTimes();
                if (plantTimes>=limit){
                    return;
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
    public JSONObject   getJdInfo() {
        JSONObject infos = new JSONObject();
        List<JSONObject> jdDatas = jdJdMapper.queryJd();
        //京豆收益
        TreeMap<String, String> jdIncome = new TreeMap<>();
        for (JSONObject jdData : jdDatas) {
            jdIncome.put(DateUtil.format(DateUtil.parse(jdData.getString("date")), "MM.dd"),jdData.getString("jdCount"));
        }
        infos.put("jdIncome", jdIncome);
        //ck总数
        Integer counts = jdCkMapper.queryCkCount();
        infos.put("allCkCount", counts);
        //东东农场
        int fruitHelp = jdFruitMapper.getHelpTimes();
        infos.put("jdfruit", "已助力满==>" + fruitHelp + "个");
        //农场抽奖
        int fruitHelpLottery = jdFruitMapper.getHelpLotteryTimes();
        infos.put("jdfruitLottery", "已助力满==>" + fruitHelpLottery + "个");
        //东东萌宠
        int petTimes = jdPetMapper.getHelpTimes();
        infos.put("jdpet", "已助力满==>" + petTimes + "个");
        //种豆得豆
        int plantTimes = jdPlantMapper.getHelpTimes();
        infos.put("jdbean", "已助力满==>" + plantTimes + "个");
        //东东工厂
        infos.put("jdddfactory", "待开发");
        //健康社区
        infos.put("jdhealth", "待开发");
        //京喜工厂
        infos.put("jdjxfactory", "待开发");
        //闪购盲盒
        infos.put("jdsgmh", "待开发");
        //京东登陆地址
        infos.put("jdLoginUrl", "http://121.43.32.165:6702");
        return infos;
    }


    /**
     * 统计京豆收益
     */
    @Async("asyncServiceExecutor")
    @Override
    public void countJd() {
        log.info("统计京豆收益开始！！！");
        int pageSize = 50;
        //昨天
        DateTime date = DateUtil.offsetDay(DateUtil.date(), -1);
        Long begin = DateUtil.beginOfDay(date).getTime();
        Long end = DateUtil.endOfDay(date).getTime();
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryCksByLevels("0,1,2");
        log.info("共查询{}个ck", jdCkEntities.size());
        Collections.shuffle(jdCkEntities);
        for (JdCkEntity jdCkEntity : jdCkEntities) {
            int page = 1;
            int today = 0;
            int yesterday = 0;
            int t = 0;
            do {
                log.info("统计"+jdCkEntity.getRemark()+"京豆中。。。");
                JSONObject jingBeanBalanceDetail = getJingBeanBalanceDetail(jdCkEntity.getCk(),page,pageSize);
                if (jingBeanBalanceDetail != null){
                    if ("0".equals(jingBeanBalanceDetail.getString("code"))){
                        page++;
                        List<JSONObject> detailList = jingBeanBalanceDetail.getJSONArray("detailList").toJavaList(JSONObject.class);
                        for (JSONObject detail : detailList) {
                            Long time = DateUtil.parse(detail.getString("date")).getTime();
                        /*if (time >= end){
                            //今天

                        }*/
                            if (time <= end && time >= begin){
                                //昨天
                                String eventMassage = detail.getString("eventMassage");
                                if (!eventMassage.contains("退还") && !eventMassage.contains("物流") && !eventMassage.contains("扣赠")){
                                    int amount = Integer.parseInt(detail.getString("amount"));
                                    if (amount>0){
                                        yesterday += amount;

                                    }
                                }
                            }
                            if (time < begin){
                                //前天跳出
                                t=1;
                            }
                        }
                    }else if ("3".equals(jingBeanBalanceDetail.getString("code"))){
                        log.info("ck已过期，或者填写不规范");
                        //跳出
                        t=1;
                    }else {
                        log.info("未知情况：{}",jingBeanBalanceDetail.toJSONString());
                        t=1;
                    }
                }else {
                    //跳出
                    t=1;
                }
                try {
                    log.info("休息5s防止黑ip...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (t==0);
            log.info("统计"+jdCkEntity.getRemark()+"京豆结果：{}",yesterday);
            jdCkEntity.setJd(yesterday);
            jdCkEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
            jdCkMapper.updateCk(jdCkEntity);
        }
        log.info("统计京豆收益结束！！！");
        log.info("更新京豆数据！！！");
        int jdCount = jdCkMapper.sumJdCount();
        int i = jdJdMapper.addJdDate(DateUtil.formatDateTime(date), jdCount);
        if (i==1){
            log.info("京豆数据更新成功！！！");
        }else {
            log.info("京豆数据更新失败！！！");
        }
    }

    public JSONObject getJingBeanBalanceDetail(String ck,int page,int pageSize){
        String result = HttpRequest.post("https://api.m.jd.com/client.action?functionId=getJingBeanBalanceDetail")
                .body("body=%7B%22pageSize%22%3A%22"+pageSize+"%22%2C%22page%22%3A%22"+page+"%22%7D&appid=ld")
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("cookie", ck)
                .timeout(10000)
                .execute().body();
        log.info("查询京豆详情结果：{}", result);
        if (StringUtils.isEmpty(result)) {
            log.info("查询京豆详情请求失败 ‼️‼️");
            return null;
        }else if(result.contains("response status: 403")){
            return null;
        } else {
            return JSONObject.parseObject(result);
        }
    }

    /**
     * 维护东东农场互助码
     */
    @Override
    @Async("asyncServiceExecutor")
    public void updateFruitShareCode(List<JdCkEntity> cks) {
        for (JdCkEntity ck : cks) {
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdFruitEntity jdFruitEntity = ck.getJdFruitEntity();
            if (jdFruitEntity == null){
                jdFruitEntity = new JdFruitEntity();
                jdFruitEntity.setCkId(ck.getId());
                jdFruitEntity.setIsFruitHei(0);
            }
            if (StringUtils.isEmpty(jdFruitEntity.getHelpCode())){
                try {
                    //查询农场信息
                    JSONObject farmInfo = getFarmInfo(ck.getCk());
                    if (farmInfo != null) {
                        if (StringUtils.isNotEmpty(farmInfo.getString("code")) && "403".equals(farmInfo.getString("code"))){
                            log.info("ip已黑，休息20s!!!");
                        }else {
                            JSONObject farmUserPro = farmInfo.getJSONObject("farmUserPro");
                            if (farmUserPro == null) {
                                log.info("{}ck的东东农场互助码更新失败，活动没开或者活动已黑", ck.getCk());
//                                jdFruitEntity.setIsFruitHei(1);
                            } else {
                                //互助码
                                String fruitShareCode = farmUserPro.getString("shareCode");
                                if (StringUtils.isNotEmpty(fruitShareCode)) {
                                    //更新农场互助码
                                    jdFruitEntity.setHelpCode(fruitShareCode);
                                    jdFruitEntity.setIsFruitHei(0);
                                }
                            }
                        }
                    }
                    if (jdFruitEntity.getId()==null){
                        //id不存在，新增
                        jdFruitMapper.addJdFruit(jdFruitEntity);
                    }else {
                        //id存在，修改
                        jdFruitMapper.updateJdFruit(jdFruitEntity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("{}的东东农场互助码更新失败，{}", ck.getCk(), e.getMessage());
                }
            }
        }
    }

    /**
     * 维护东东萌宠互助码
     */
    @Override
    @Async("asyncServiceExecutor")
    public void updatePetShareCode(List<JdCkEntity> cks) {
        for (JdCkEntity ck : cks) {
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdPetEntity jdPetEntity = ck.getJdPetEntity();
            if (jdPetEntity == null){
                jdPetEntity = new JdPetEntity();
                jdPetEntity.setCkId(ck.getId());
                jdPetEntity.setIsPetHei(0);
            }
            if (StringUtils.isEmpty(jdPetEntity.getHelpCode())){
                try {
                    //查询萌宠信息
                    JSONObject petInfo = getPetInfo(ck.getCk(),"initPetTown");
                    if (petInfo != null) {
                        if (StringUtils.isNotEmpty(petInfo.getString("code")) && "403".equals(petInfo.getString("code"))){
                            log.info("ip已黑，休息20s!!!");
                        }else {
                            if ("0".equals(petInfo.getString("code")) && "0".equals(petInfo.getString("resultCode")) && "success".equals(petInfo.getString("message"))){
                                JSONObject petUserPro = petInfo.getJSONObject("result");
                                if (petUserPro==null
                                        || "0".equals(petUserPro.getString("userStatus"))
                                        || "0".equals(petUserPro.getString("petStatus"))
                                        || "0".equals(petUserPro.getString("petStatus"))
                                        || StringUtils.isEmpty(petUserPro.getString("goodsInfo"))){
                                    log.info("{}ck的东东萌宠互助码更新失败，活动没开或者活动已黑", ck.getCk());
//                                    jdPetEntity.setIsPetHei(1);
                                }else {
                                    //互助码
                                    String petShareCode = petUserPro.getString("shareCode");
                                    if (StringUtils.isNotEmpty(petShareCode)) {
                                        //更新农场互助码
                                        jdPetEntity.setHelpCode(petShareCode);
                                        jdPetEntity.setIsPetHei(0);
                                    }
                                }
                            }
                        }
                    }
                    if (jdPetEntity.getId()==null){
                        //id不存在，新增
                        jdPetMapper.addJdPet(jdPetEntity);
                    }else {
                        //id存在，修改
                        jdPetMapper.updateJdPet(jdPetEntity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("{}的东东萌宠互助码更新失败，{}", ck.getCk(), e.getMessage());
                }
            }
        }
    }

    /**
     * 维护种豆得豆互助码
     */
    @Override
    @Async("asyncServiceExecutor")
    public void updatePlantShareCode(List<JdCkEntity> cks) {
        for (JdCkEntity ck : cks) {
            if (ck.getStatus()==1){
                //ck失效
                continue;
            }
            JdPlantEntity jdPlantEntity = ck.getJdPlantEntity();
            if (jdPlantEntity == null){
                jdPlantEntity = new JdPlantEntity();
                jdPlantEntity.setCkId(ck.getId());
                jdPlantEntity.setIsPlantHei(0);
            }
            if (StringUtils.isEmpty(jdPlantEntity.getHelpCode())){
                try {
                    //查询种豆得豆信息
                    JSONObject plantInfo = getPlantInfo(ck.getCk(),"plantBeanIndex");
                    if (plantInfo != null) {
                        if (StringUtils.isNotEmpty(plantInfo.getString("code")) && "403".equals(plantInfo.getString("code"))){
                            log.info("ip已黑，休息20s!!!");
                        }else {
                            if ("PB101".equals(plantInfo.getString("errorCode")) || "3".equals(plantInfo.getString("code"))){
                                log.info("{}ck的种豆得豆互助码更新失败，活动没开或者活动已黑", ck.getCk());
//                                jdPlantEntity.setIsPlantHei(1);
                            }else {
                                if ("0".equals(plantInfo.getString("code")) && StringUtils.isNotEmpty(plantInfo.getString("data"))){
                                    String shareUrl = plantInfo.getJSONObject("data").getJSONObject("jwordShareInfo").getString("shareUrl");
                                    String plantShareCode = shareUrl.split("plantUuid=")[1];
                                    jdPlantEntity.setHelpCode(plantShareCode);
                                }
                            }
                        }
                    }
                    if (jdPlantEntity.getId()==null){
                        //id不存在，新增
                        jdPlantMapper.addJdPlant(jdPlantEntity);
                    }else {
                        //id存在，修改
                        jdPlantMapper.updateJdPlant(jdPlantEntity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("{}的种豆得豆互助码更新失败，{}", ck.getCk(), e.getMessage());
                }
            }
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
        log.info("{}开始助力东东农场->{}", toHelpJdFruitEntity.getHelpCode(), jdFruitEntity.getHelpCode());
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
            if ("0".equals(resultObject.getString("code"))) {
                JSONObject helpResult = resultObject.getJSONObject("helpResult");
                if ("0".equals(helpResult.getString("code"))) {
                    //助力成功
                    log.info("【东东农场助力好友结果】: 已成功助力");
                } else if ("8".equals(helpResult.getString("code"))) {
                    log.info("【东东农场助力好友结果】: 助力失败，您今天助力次数已耗尽");
                    toHelpJdFruitEntity.setToHelpStatus(0);
                    jdFruitMapper.updateJdFruit(toHelpJdFruitEntity);
                } else if ("9".equals(helpResult.getString("code"))) {
                    log.info("【东东农场助力好友结果】: 之前助力过了");
                } else if ("10".equals(helpResult.getString("code"))) {
                    log.info("【东东农场助力好友结果】: 好友已满助力");
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
            } else if ("400".equals(resultObject.getString("code"))) {
                log.info("东东农场火爆 ‼️‼️");
                toHelpJdFruitEntity.setIsFruitHei(1);
//                jdFruitMapper.updateJdFruit(toHelpJdFruitEntity);
            } else if ("3".equals(resultObject.getString("code"))) {
                log.info("ck已过期 ‼️‼️");
                toHelpJdCk.setStatus(1);
                toHelpJdCk.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                jdCkMapper.updateCk(toHelpJdCk);
            }else {
                log.info("东东农场助力失败 ‼️‼️");
            }
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
            }else {
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
        log.info("{}开始助力东东萌宠->{}", toHelpJdPetEntity.getHelpCode(), jdPetEntity.getHelpCode());
        JSONObject body = new JSONObject();
        body.put("shareCode", jdPetEntity.getHelpCode());
        body.put("version", 2);
        body.put("channel", "app");
        String result = HttpRequest.post(JDAPIHOST + "?functionId=slaveHelp")
                .body("body="+ EscapeUtil.escape(body.toJSONString()) +"&appid=wh5&loginWQBiz=pet-town&clientVersion=9.0.4")
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
            if ("0".equals(resultObject.getString("code")) && "0".equals(resultObject.getString("resultCode"))) {
                JSONObject helpResult = resultObject.getJSONObject("result");
                if ("0".equals(helpResult.getString("helpStatus"))) {
                    //助力成功
                    log.info("【东东萌宠助力好友结果】: 已成功助力");
                } else if ("1".equals(helpResult.getString("helpStatus"))) {
                    log.info("【东东萌宠助力好友结果】: 助力失败，您今天助力次数已耗尽");
                    toHelpJdPetEntity.setToHelpStatus(0);
                    jdPetMapper.updateJdPet(toHelpJdPetEntity);
                }  else if ("2".equals(helpResult.getString("helpStatus"))) {
                    log.info("【东东萌宠助力好友结果】: 好友已满助力");
                    jdPetEntity.setHelpStatus(1);
                    jdPetMapper.updateJdPet(jdPetEntity);
                } else {
                    log.info("东东萌宠助力其他情况：{}", helpResult.toJSONString());
                }
            } else if ("已经助过力".equals(resultObject.getString("message"))) {
                log.info("此账号今天已经跑过助力了，跳出....");
            } else if ("1002".equals(resultObject.getString("resultCode"))) {
                toHelpJdPetEntity.setIsPetHei(1);
//                jdPetMapper.updateJdPet(toHelpJdPetEntity);
                log.info("此账号风控，跳出....");
            } else {
                log.info("东东萌宠助力失败:{}",resultObject.toJSONString());
            }
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
        log.info("{}开始助力种豆得豆->{}", toHelpJdPlantEntity.getHelpCode(), jdPlantEntity.getHelpCode());
        JSONObject body = new JSONObject();
        body.put("plantUuid", jdPlantEntity.getHelpCode());
        body.put("wxHeadImgUrl", "");
        body.put("shareUuid", "");
        body.put("followType", "1");
        String result = HttpRequest.post(JDAPIHOST)
                .body("functionId=plantBeanIndex&body="+ URLEncodeUtil.encode(body.toJSONString()) +"&appid=ld&client=apple&area=19_1601_50258_51885&build=167490&clientVersion=9.3.2")
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
            if ("0".equals(resultObject.getString("code")) && StringUtils.isNotEmpty(resultObject.getString("data"))) {
                JSONObject helpResult = resultObject.getJSONObject("data");
                if ("1".equals(helpResult.getJSONObject("helpShareRes").getString("state"))) {
                    //助力成功
                    log.info("【种豆得豆助力好友结果】: 已成功助力");
                } else if ("2".equals(helpResult.getJSONObject("helpShareRes").getString("state"))) {
                    log.info("【种豆得豆助力好友结果】: 助力失败，您今天助力次数已耗尽");
                    toHelpJdPlantEntity.setToHelpStatus(0);
                    jdPlantMapper.updateJdPlant(toHelpJdPlantEntity);
                }  else if ("3".equals(helpResult.getJSONObject("helpShareRes").getString("state"))) {
                    log.info("【种豆得豆助力好友结果】: 好友已满助力");
                    jdPlantEntity.setHelpStatus(1);
                    jdPlantMapper.updateJdPlant(jdPlantEntity);
                } else {
                    log.info("种豆得豆助力其他情况：{}", helpResult.toJSONString());
                }
            } else {
                log.info("东东萌宠助力失败:{}",resultObject.toJSONString());
            }
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
        try {
            log.info("休息20s防止黑ip...");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public JSONObject getPetInfo(String ck,String functionId) {
        JSONObject body = new JSONObject();
        body.put("version",2);
        body.put("channel","app");
        String result = HttpRequest.post(JDAPIHOST + "?functionId="+functionId)
                .body("body="+ EscapeUtil.escape(body.toJSONString()) +"&appid=wh5&loginWQBiz=pet-town&clientVersion=9.0.4")
                .header("cookie", ck)
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Host", "api.m.jd.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(10000)
                .execute().body();
        log.info("查询萌宠信息结果：{}", result);
        try {
            log.info("休息20s防止黑ip...");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public JSONObject getPlantInfo(String ck,String functionId) {
        JSONObject body = new JSONObject();
        body.put("version","9.2.4.1");
        body.put("monitor_source","plant_app_plant_index");
        body.put("monitor_refer","");
        String result = HttpRequest.post(JDAPIHOST)
                .body("functionId="+functionId+"&body="+ URLEncodeUtil.encode(body.toJSONString()) +"&appid=ld&client=apple&area=19_1601_50258_51885&build=167490&clientVersion=9.3.2")
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
            log.info("休息20s防止黑ip...");
            Thread.sleep(20000);
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
     * @return
     */
    public boolean initForTurntableFarm(String cookie){
        JSONObject body = new JSONObject();
        body.put("version",4);
        body.put("channel",1);
        String functionId = "initForTurntableFarm";
        String resStr = get(functionId, body.toJSONString(), cookie);
        log.info("初始化天天抽奖得好礼返回：{}",resStr);
        if (StringUtils.isEmpty(resStr)){
            log.info("初始化天天抽奖得好礼失败");
            return false;
        }else {
            JSONObject res = JSONObject.parseObject(resStr);
            if ("0".equals(res.getString("code"))){
                return true;
            }else {
                log.info("初始化天天抽奖得好礼失败");
                return false;
            }
        }
    }

    public String get(String functionId,String body,String cookie){
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

    /**
     * 查询京东资产详情
     * @param ptPin
     */
    public void getJdBeanChange(String ptPin){
        String cookie = "";

        /*JdCkEntity jdCk = jdCkMapper.queryCk();
        $.overdue = "";
        var overdueDate = moment(cookiesArr[i].CreateTime);
        var day = moment(new Date()).diff(overdueDate, 'day');
        $.overdue = `【挂机天数】${day}天`
        $.pt_pin = (cookie.match(/pt_pin=([^; ]+)(?=;?)/) && cookie.match(/pt_pin=([^; ]+)(?=;?)/)[1]);
        $.UserName = decodeURIComponent(cookie.match(/pt_pin=([^; ]+)(?=;?)/) && cookie.match(/pt_pin=([^; ]+)(?=;?)/)[1]);
        $.CryptoJS = $.isNode() ? require('crypto-js') : CryptoJS;
        $.index = i + 1;
        $.beanCount = 0;
        $.incomeBean = 0;
        $.expenseBean = 0;
        $.todayIncomeBean = 0;
        $.todayOutcomeBean = 0;
        $.errorMsg = '';
        $.isLogin = true;
        $.nickName = '';
        $.levelName = '';
        $.message = '';
        $.balance = 0;
        $.expiredBalance = 0;
        $.JdzzNum = 0;
        $.JdMsScore = 0;
        $.JdFarmProdName = '';
        $.JdtreeEnergy = 0;
        $.JdtreeTotalEnergy = 0;
        $.treeState = 0;
        $.JdwaterTotalT = 0;
        $.JdwaterD = 0;
        $.JDwaterEveryDayT = 0;
        $.JDtotalcash = 0;
        $.JDEggcnt = 0;
        $.Jxmctoken = '';
        $.DdFactoryReceive = '';
        $.jxFactoryInfo = '';
        $.jxFactoryReceive = '';
        $.jdCash = 0;
        $.isPlusVip = 0;
        $.JingXiang = "";
        $.allincomeBean = 0; //月收入
        $.allexpenseBean = 0; //月支出
        $.joylevel = 0;
        TempBaipiao = "";*/
    }

}
