package com.bienao.robot.service.impl.jingdong;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.mapper.jingdong.JdFruitMapper;
import com.bienao.robot.mapper.jingdong.JdPetMapper;
import com.bienao.robot.mapper.jingdong.JdPlantMapper;
import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.utils.jingdong.GetUserAgentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * 查询当前ck信息
     *
     * @param ck
     * @return
     */
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
        if (StringUtils.isEmpty(result)) {
            log.info("京东服务器返回空数据");
            throw new RuntimeException("京东服务器返回空数据");
        } else {
            JSONObject resultObject = JSONObject.parseObject(result);
            if (resultObject.getString("retcode").equals("1001")) {
                //ck过期
                return null;
            }
            if (resultObject.getString("retcode").equals("0") && resultObject.getJSONObject("data").getJSONObject("userInfo") != null) {
                return resultObject.getJSONObject("data").getJSONObject("userInfo");
            }
            return new JSONObject();
        }
    }

    /**
     * 添加ck
     *
     * @param ck
     * @param level
     * @return
     */
    @Override
    public boolean addCk(String ck, int level) {
        //判断ck是否已经添加
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        jdCkEntityQuery.setCk(ck);
        JdCkEntity jdCkEntity = jdCkMapper.queryCk(jdCkEntityQuery);
        if (jdCkEntity != null) {
            throw new RuntimeException("ck已经添加过");
        }

        //校验ck是否有效
        JSONObject jsonObject = queryDetail(ck);
        if (jsonObject==null){
            throw new RuntimeException("ck不存在或者ck已失效");
        }

        String pt_pin = "";
        Pattern compile = Pattern.compile("pt_pin=(.+?);");
        Matcher matcher = compile.matcher(ck);
        if (matcher.find()){
            pt_pin = matcher.group(1);
        }

        //判断pt_pin是否存在
        jdCkEntityQuery = new JdCkEntity();
        jdCkEntityQuery.setPtPin(pt_pin);
        jdCkEntity = jdCkMapper.queryCk(jdCkEntityQuery);
        if (jdCkEntity == null) {
            //添加
            jdCkEntity = new JdCkEntity();
            jdCkEntity.setCk(ck);
            jdCkEntity.setLevel(level);
            jdCkEntity.setPtPin(pt_pin);
            int i = jdCkMapper.addCk(jdCkEntity);
            if (i != 0) {
                return true;
            }
            return false;
        }else {
            //更新
            jdCkEntity.setCk(ck);
            jdCkEntity.setUpdatedTime(new Date());
            int i = jdCkMapper.updateCk(jdCkEntity);
            if (i != 0) {
                return true;
            }
            return false;
        }
    }

    /**
     * 每天凌晨重置ck的助力数据
     */
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
     * 检查ck是否过期
     */
    @Override
    public void checkCk() {
        log.info("定时检查ck开始。。。");
        JdCkEntity jdCkEntityQuery = new JdCkEntity();
        List<JdCkEntity> jdCkEntities = jdCkMapper.queryCks(jdCkEntityQuery);
        log.info("共查询{}个ck", jdCkEntities.size());
        for (JdCkEntity jdCkEntity : jdCkEntities) {
            JSONObject jsonObject = queryDetail(jdCkEntity.getCk());
            if (jsonObject == null) {
                //清理过期ck
                log.info("{}已过期，删除！！！",jdCkEntity.getCk());
                jdCkEntity.setStatus(1);
                jdCkMapper.updateCk(jdCkEntity);
            } else {
                if (StringUtils.isEmpty(jdCkEntity.getRemark())){
                    JSONObject baseInfo = jsonObject.getJSONObject("baseInfo");
                    if (baseInfo != null) {
                        jdCkEntity.setRemark(baseInfo.getString("nickname"));
                        jdCkEntity.setUpdatedTime(new Date());
                        jdCkMapper.updateCk(jdCkEntity);
                    }
                }
                //查看助力超级vip是否过期
                if (jdCkEntity.getLevel()==1 && jdCkEntity.getExpiryTime() != null){
                    Date expiryTime = jdCkEntity.getExpiryTime();
                    if (expiryTime.getTime() < DateUtil.date().getTime()){
                        jdCkEntity.setLevel(2);
                        jdCkEntity.setUpdatedTime(new Date());
                        jdCkEntity.setExpiryTime(null);
                        jdCkMapper.updateCk(jdCkEntity);
                    }
                }
            }
            try {
                log.info("休息20s防止黑ip...");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("定时清理过期ck结束。。。");
    }
}
