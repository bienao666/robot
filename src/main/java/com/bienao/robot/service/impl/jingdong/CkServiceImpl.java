package com.bienao.robot.service.impl.jingdong;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.QlEnv;
import com.bienao.robot.entity.Result;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.mapper.jingdong.JdFruitMapper;
import com.bienao.robot.mapper.jingdong.JdPetMapper;
import com.bienao.robot.mapper.jingdong.JdPlantMapper;
import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.service.jingdong.JdDhService;
import com.bienao.robot.utils.jingdong.GetUserAgentUtil;
import com.bienao.robot.utils.jingdong.JDUtil;
import com.bienao.robot.utils.ql.QlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CkServiceImpl implements CkService {

    @Autowired
    private JdCkMapper jdCkMapper;

    @Autowired
    private JdFruitMapper jdFruitMapper;

    @Autowired
    private JdPetMapper jdPetMapper;

    @Autowired
    private JdPlantMapper jdPlantMapper;

    @Autowired
    private JdDhService jdDhService;

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private QlMapper qlMapper;

    private Pattern jdPinPattern = Pattern.compile("pt_pin=(.+?);");

    /**
     * 查询当前ck信息
     *
     * @param ck
     * @return
     */
    @Override
    public JSONObject queryDetail(String ck) {
        String result = HttpRequest.get("https://me-api.jd.com/user_new/info/GetJDUserInfoUnion")
                .header("Host", "me-api.jd.com")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .header("Cookie", ck)
                .header("User-Agent", GetUserAgentUtil.getUserAgent())
                .header("Accept-Language", "zh-cn")
                .header("Referer", "https://home.m.jd.com/myJd/newhome.action?sceneval=2&ufc=&")
                .header("Accept-Encoding", "gzip, deflate, br")
                .execute().body();
        log.info("查询当前ck结果：{}", result);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(result)) {
            log.info("京东服务器返回空数据");
            throw new RuntimeException("京东服务器返回空数据");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            if ("1001".equals(resultObject.getString("retcode"))) {
                //ck过期
                return null;
            }
            if ("0".equals(resultObject.getString("retcode")) && resultObject.getJSONObject("data").getJSONObject("userInfo") != null) {
                return resultObject.getJSONObject("data").getJSONObject("userInfo");
            }
            return new JSONObject();
        }
    }

    /**
     * 添加ck
     *
     * @param jdCkEntity
     * @return
     */
    @Override
    public boolean addCk(JdCkEntity jdCkEntity) {
        //判断ck是否已经添加
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        jdCkEntityQuery.setCk(jdCkEntity.getCk());
        JdCkEntity jdck = jdCkMapper.queryCk(jdCkEntityQuery);
        if (jdck != null) {
            throw new RuntimeException("ck已经添加过");
        }

        //校验ck是否有效
        JSONObject jsonObject = queryDetail(jdCkEntity.getCk());
        if (jsonObject == null) {
            throw new RuntimeException("ck不存在或者ck已失效");
        }

        String ptPin = "";
        Matcher matcher = jdPinPattern.matcher(jdCkEntity.getCk());
        if (matcher.find()) {
            ptPin = matcher.group(1);
        }
        jdCkEntity.setPtPin(ptPin);

        //判断pt_pin是否存在
        jdCkEntityQuery = new JdCkEntity();
        jdCkEntityQuery.setPtPin(ptPin);
        jdck = jdCkMapper.queryCk(jdCkEntityQuery);
        if (jdck == null) {
            //添加
            int i = jdCkMapper.addCk(jdCkEntity);
            if (i != 0) {
                return true;
            }
            return false;
        } else {
            //更新
            jdck.setCk(jdCkEntity.getCk());
            jdck.setUpdatedTime(DateUtil.formatDateTime(new Date()));
            jdck.setStatus(0);
            if (StringUtils.isNotEmpty(jdCkEntity.getRemark())) {
                jdck.setRemark(jdCkEntity.getRemark());
            }
            int i = jdCkMapper.updateCk(jdck);
            if (i != 0) {
                return true;
            }
            return false;
        }
    }

    /**
     * 添加ck无需验证
     *
     * @param jdCkEntity
     * @return
     */
    @Override
    public boolean addCkWithOutCheck(JdCkEntity jdCkEntity) {
        //判断ck是否已经添加
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        jdCkEntityQuery.setPtPin(jdCkEntity.getPtPin());
        JdCkEntity jdck = jdCkMapper.queryCk(jdCkEntityQuery);
        if (jdck == null) {
            //添加
            int i = jdCkMapper.addCk(jdCkEntity);
            if (i != 0) {
                return true;
            }
            return false;
        } else {
            //更新
            jdck.setCk(jdCkEntity.getCk());
            jdck.setUpdatedTime(DateUtil.formatDateTime(new Date()));
            jdck.setStatus(0);
            if (StringUtils.isNotEmpty(jdCkEntity.getRemark())) {
                jdck.setRemark(jdCkEntity.getRemark());
            }
            int i = jdCkMapper.updateCk(jdck);
            if (i != 0) {
                return true;
            }
            return false;
        }
    }

    /**
     * 每天凌晨重置ck的助力数据
     */
    @Override
    public void resetCkStatus() {
        log.info("开始重置所有ck助力数据。。。");
        int fruit = jdFruitMapper.resetFruitStatus();
        log.info("东东农场重置完成，共重置{}个ck", fruit);
        int pet = jdPetMapper.resetPetStatus();
        log.info("东东萌宠重置完成，共重置{}个ck", pet);
        int plant = jdPlantMapper.resetPlantStatus();
        log.info("种豆得豆重置完成，共重置{}个ck", plant);
        int jd = jdCkMapper.resetJd();
        log.info("京豆收益重置完成，共重置{}个ck", jd);
    }

    /**
     * 检查助力是否过期
     */
    @Override
    public void checkZlc() {
        log.info("定时检查助力是否过期开始。。。");
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryCks(jdCkEntityQuery);
        log.info("共查询{}个ck", jdCkEntities.size());
        for (JdCkEntity jdCkEntity : jdCkEntities) {
            if (StringUtils.isEmpty(jdCkEntity.getRemark())) {
                JSONObject jsonObject = queryDetail(jdCkEntity.getCk());
                if (jsonObject!=null){
                    JSONObject baseInfo = jsonObject.getJSONObject("baseInfo");
                    if (baseInfo != null) {
                        jdCkEntity.setRemark(baseInfo.getString("nickname"));
                        jdCkEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                        jdCkMapper.updateCk(jdCkEntity);
                    }
                }
            }
            //查看助力超级vip是否过期
            if (jdCkEntity.getLevel() == 1 && StringUtils.isNotEmpty(jdCkEntity.getExpiryTime())) {
                DateTime expiryTime = DateUtil.parse(jdCkEntity.getExpiryTime());
                if (expiryTime.getTime() < DateUtil.date().getTime()) {
                    jdCkEntity.setLevel(2);
                    jdCkEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
                    jdCkEntity.setExpiryTime(null);
                    jdCkMapper.updateCk(jdCkEntity);
                }
            }
            try {
                log.info("休息20s防止黑ip...");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("定时检查助力是否过期结束。。。");
    }

    @Override
    public Result getJdCks(String ck, Integer level, Integer status,String remark, Integer pageNo, Integer pageSize) {
        JdCkEntity jdCkEntity = new JdCkEntity();
        jdCkEntity.setCk(ck);
        jdCkEntity.setLevel(level);
        jdCkEntity.setStatus(status);
        jdCkEntity.setRemark(remark);
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryCks(jdCkEntity);
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", jdCkEntities.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        jdCkEntities = jdCkEntities.subList(start, Math.min(end, jdCkEntities.size()));
        result.put("jdCkList", jdCkEntities);
        return Result.success(result);
    }

    @Override
    public Result updateJdCk(JdCkEntity jdCkEntity) {
        int i = jdCkMapper.updateCk(jdCkEntity);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR, "数据库操作异常");
        }
    }

    @Override
    public Result deleteJdCks(List<Integer> ids) {
        int i = jdCkMapper.deleteCks(ids);
        if (i > 0) {
            return Result.success();
        } else {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR, "数据库操作异常");
        }
    }

    @Override
    public Result getJdCkList(String ck, String ptPin, Integer status, String qlName, Integer pageNo, Integer pageSize) {
        List<JdCkEntity> jdCkEntities = new ArrayList<>();
        //查询所有青龙
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (QlEntity qlEntity : qlEntities) {
            if (StringUtils.isNotEmpty(qlName) && !qlEntity.getRemark().contains(qlName)) {
                continue;
            }
            try {
                List<QlEnv> envs = qlUtil.getEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
                for (int i = 0; i < envs.size(); i++) {
                    QlEnv env = envs.get(i);
                    if ("JD_COOKIE".equals(env.getName())) {
                        if (StringUtils.isNotEmpty(ck) && !env.getValue().contains(ck)) {
                            continue;
                        }
                        if (StringUtils.isNotEmpty(ptPin) && !env.getValue().contains(ptPin)) {
                            continue;
                        }
                        if (status != null && !status.equals(env.getStatus())) {
                            continue;
                        }
                        JdCkEntity jdCkEntity = new JdCkEntity();
                        jdCkEntity.setQlId(qlEntity.getId());
                        jdCkEntity.setQlRemark(qlEntity.getRemark());
                        jdCkEntity.setId(env.getId());
                        jdCkEntity.setCk(env.getValue());
                        jdCkEntity.setRemark(env.getRemarks());
                        jdCkEntity.setStatus(env.getStatus());
                        jdCkEntity.setQlindex(i+1);
                        //查询前一天的收益
                        JdCkEntity query = new JdCkEntity();
                        query.setCk(env.getValue());
                        JdCkEntity queryRes = jdCkMapper.queryCk(query);
                        if (queryRes!=null){
                            jdCkEntity.setJd(queryRes.getJd());
                        }
                        jdCkEntities.add(jdCkEntity);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        jdCkEntities = jdCkEntities.stream().sorted(Comparator.comparing(JdCkEntity::getJd).reversed()).collect(Collectors.toList());
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", jdCkEntities.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        jdCkEntities = jdCkEntities.subList(start, Math.min(end, jdCkEntities.size()));
        result.put("jdCkList", jdCkEntities);
        return Result.success(result);
    }

    @Override
    @Async("asyncServiceExecutor")
    public void jkExchange(){
        //查询所有青龙
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (QlEntity qlEntity : qlEntities) {
            List<JdCkEntity> jdCkEntities = new ArrayList<>();
            try {
                List<QlEnv> envs = qlUtil.getEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
                for (int i = 0; i < envs.size(); i++) {
                    QlEnv env = envs.get(i);
                    if ("JD_COOKIE".equals(env.getName())) {
                        JdCkEntity jdCkEntity = new JdCkEntity();
                        jdCkEntity.setQlId(qlEntity.getId());
                        jdCkEntity.setQlRemark(qlEntity.getRemark());
                        jdCkEntity.setId(env.getId());
                        jdCkEntity.setCk(env.getValue());
                        jdCkEntity.setRemark(env.getRemarks());
                        jdCkEntity.setStatus(env.getStatus());
                        jdCkEntity.setQlindex(i);
                        jdCkEntities.add(jdCkEntity);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //健康社区兑换京豆
            jdDhService.jkExchange(jdCkEntities);
        }
    }

    @Override
    public Result enableJdCk(List<JSONObject> cks) {
        HashMap<Integer, List<Integer>> map = new HashMap<>();
        for (JSONObject ck : cks) {
            Integer qlId = ck.getInteger("qlId");
            Integer id = ck.getInteger("id");
            List<Integer> list = map.get(qlId);
            if (list == null) {
                list = new ArrayList<>();
                list.add(id);
            } else {
                list.add(id);
            }
            map.put(qlId,list);
        }
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer qlId = entry.getKey();
            QlEntity query = new QlEntity();
            query.setId(qlId);
            QlEntity qlEntity = qlMapper.queryQl(query);
            if (qlEntity != null) {
                List<Integer> list = entry.getValue();
                qlUtil.enableEnv(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken(), list);
            }
        }
        return Result.success();
    }

    @Override
    public Result disableJdCk(List<JSONObject> cks) {
        HashMap<Integer, List<Integer>> map = new HashMap<>();
        for (JSONObject ck : cks) {
            Integer qlId = ck.getInteger("qlId");
            Integer id = ck.getInteger("id");
            List<Integer> list = map.get(qlId);
            if (list == null) {
                list = new ArrayList<>();
                list.add(id);
            } else {
                list.add(id);
            }
            map.put(qlId,list);
        }
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer qlId = entry.getKey();
            QlEntity query = new QlEntity();
            query.setId(qlId);
            QlEntity qlEntity = qlMapper.queryQl(query);
            if (qlEntity != null) {
                List<Integer> list = entry.getValue();
                qlUtil.disableEnv(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken(), list);
            }
        }
        return Result.success();
    }

    @Override
    public Result deleteJdCk(List<JSONObject> cks) {
        HashMap<Integer, List<Integer>> map = new HashMap<>();
        for (JSONObject ck : cks) {
            Integer qlId = ck.getInteger("qlId");
            Integer id = ck.getInteger("id");
            List<Integer> list = map.get(qlId);
            if (list == null) {
                list = new ArrayList<>();
                list.add(id);
            } else {
                list.add(id);
            }
            map.put(qlId,list);
        }
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer qlId = entry.getKey();
            QlEntity query = new QlEntity();
            query.setId(qlId);
            QlEntity qlEntity = qlMapper.queryQl(query);
            if (qlEntity != null) {
                List<Integer> list = entry.getValue();
                qlUtil.deleteEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken(), list);
            }
        }
        return Result.success();
    }

    /**
     * 过期ck
     */
    @Override
    public Result expireCk(List<String> cks) {
        for (String ck : cks) {
            JDUtil.expire(ck);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

}
