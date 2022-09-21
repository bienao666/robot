package com.bienao.robot.service.impl.ql;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.*;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.mapper.WireKeyMapper;
import com.bienao.robot.mapper.WireMapper;
import com.bienao.robot.mapper.WirelistMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.utils.ql.QlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class WireServiceImpl implements WireService {

    @Autowired
    private WireMapper wireMapper;

    @Autowired
    private WireKeyMapper wireKeyMapper;

    @Autowired
    private QlMapper qlMapper;

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private WirelistMapper wirelistMapper;

    /**
     * 添加线报活动
     * @param wireEntity
     * @return
     */
    @Override
    @Transactional
    public Result addWire(WireEntity wireEntity) {
        int i = wireMapper.addWire(wireEntity);
        if (i==0) {
            throw new RuntimeException("数据库操作异常");
        }
        Integer maxId = wireMapper.queryMaxId();
        List<WireKeyEntity> keys = wireEntity.getKeys();
        for (WireKeyEntity key : keys) {
            i = wireKeyMapper.addWireKey(maxId, key.getKey());
            if (i==0){
                throw new RuntimeException("数据库操作异常");
            }
        }
        return Result.success("操作成功");
    }

    /**
     * 添加线报活动
     * @param
     * @return
     */
    @Override
    public void addWire(String activityName,String script,List<String> keys){
        try {
            WireEntity wireEntity = new WireEntity(activityName,script);
            Integer res = wireMapper.addWire(wireEntity);
            if (res!=0){
                Integer maxId = wireMapper.queryMaxId();
                for (String key : keys) {
                    wireKeyMapper.addWireKey(maxId,key);
                }
            }
        } catch (Exception e) {
            //todo
        }
        keys.clear();
    }

    /**
     * 修改线报活动
     * @param wireEntity
     */
    @Override
    public Result updateWire(WireEntity wireEntity) {
        wireEntity.setUpdatedTime(DateUtil.formatDateTime(new Date()));
        wireMapper.updateWire(wireEntity);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(wireEntity.getId());
        wireKeyMapper.deleteWireKey(ids);
        for (WireKeyEntity key : wireEntity.getKeys()) {
            wireKeyMapper.addWireKey(wireEntity.getId(),key.getKey());
        }
        return Result.success("修改成功");
    }

    /**
     * 删除线报活动
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Result deleteWire(List<Integer> ids) {
        Integer res = wireMapper.deleteWire(ids);
        if (res==0){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"删除失败");
        }else {
            res = wireKeyMapper.deleteWireKey(ids);
            if (res==0){
                throw new RuntimeException("数据库操作异常");
            }else {
                return Result.success();
            }
        }
    }

    /**
     * 执行线报活动
     * @param script
     * @param wire
     * @return
     */
    @Override
    public Result handleActivity(Integer wireListId,String script,String wire) {
        log.info("执行线报活动：{}->{}",script,wire);
        ArrayList<String> result = new ArrayList<>();
        List<String> list = Arrays.asList(wire.split("\\r?\\n"));
        ArrayList<String> keys = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            boolean configFlag = false;
            //设置参数
            try {
                String configs = qlUtil.getFile(ql.getUrl(), ql.getTokenType(), ql.getToken(), "config.sh");
                for (String config : list) {
                    if (!config.contains("export")){
                        continue;
                    }
                    if (config.contains("=")){
                        StringBuffer stringBuffer = new StringBuffer(configs);
                        //export 参数名
                        String[] split = config.split("=");
                        String s1 = split[0];
                        String key = s1.replace("export", "").replace(" ", "");
                        keys.add(key);
                        //参数值
                        String s2 = split[1];
                        if (split.length>2){
                            for (int i = 2; i < split.length; i++) {
                                s2 = s2 + "=" + split[i];
                            }
                        }
                        if (configs.contains(s1)){
                            //配置过
                            int s1index = configs.indexOf(s1);
                            int first = configs.indexOf("\"", s1index + s1.length());
                            int last = configs.indexOf("\"", first+1)+1;
                            configs = stringBuffer.replace(first,last,s2).toString();
                        }else {
                            //没配置过
                            configs += "\n" + config;
                        }
                    }
                }
                boolean flag = qlUtil.saveFile(ql.getUrl(), ql.getTokenType(), ql.getToken(), "config.sh", configs);
                if (flag){
                    //配置成功
                    result.add(ql.getUrl() + "(" + ql.getRemark() + ")" + " 配置 成功");
                    configFlag = true;
                }else {
                    //配置失败
                    result.add(ql.getUrl() + "(" + ql.getRemark() + ")" + " 配置 失败");
                    configFlag = false;
                    throw new RuntimeException("配置失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.add(ql.getUrl() + "(" + ql.getRemark() + ")" + " 线报 未执行，请手动配置执行");
            }

            if (configFlag){
                //执行脚本
                String remark = ql.getRemark();
                String url = ql.getUrl();
                try {
                    List<QlCron> crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                    if (crons == null) {
                        //重试一次
                        crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                    }
                    if (crons != null) {
                        Integer old = list.size();
                        for (QlCron cron : crons) {
                            if (cron.getCommand().contains(script)) {
                                Integer id = cron.getId();
                                List<Integer> cronIds = new ArrayList<>();
                                cronIds.add(id);
                                boolean flag = qlUtil.runCron(url, ql.getTokenType(), ql.getToken(), cronIds);
                                if (flag) {
                                    result.add(url + "(" + remark + ")" + " 线报 执行成功");
//                                    handleFlag = true;
                                } else {
                                    result.add(url + "(" + remark + ")" + " 线报 执行失败，请手动执行");
                                }
                                break;
                            }
                        }
                        Integer now = result.size();
                        if (now.equals(old)) {
                            result.add(url + "(" + remark + ")" + script + " 脚本不存在，请拉库后执行");
                        }
                    } else {
                        result.add(url + "(" + remark + ")" + " 线报 执行失败，请手动执行");
                    }

                } catch (Exception e) {
                    result.add(url + "(" + remark + ")" + "线报 执行失败，请手动执行");
                    e.printStackTrace();
                }
            }
        }
        //更新线报表
        if (result.size()!=0){
            wirelistMapper.updateWirelist(wireListId,JSONObject.toJSONString(result),new Date());
            return Result.success();
        }else {
            return Result.error(ErrorCodeConstant.SERVICE_ERROR,"线报执行异常");
        }
    }

    /**
     * 添加线报活动
     * @param wire
     * @return
     */
    @Override
    public Result addActivity(String wire) {
        String script = "";
        List<String> list = Arrays.asList(wire.split("\\r?\\n"));
        for (String config : list) {
            if (config.contains("#") && (config.contains(".js") || config.contains(".py"))){
                continue;
            }
            if (config.contains("=")){
                //export 参数名
                String s1 = config.split("=")[0];
                String key = s1.replace("export", "").replace(" ", "");
                String s = wireKeyMapper.queryScript(key);
                if (s!=null){
                    script = s;
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(script)){
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"添加失败，线报不存在，请先添加");
        }
        int i = 0;
        try {
            i = wirelistMapper.addActivity(script,wire);
        } catch (Exception e) {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"线报已存在，添加失败");
        }
        Integer maxId = wirelistMapper.queryMaxId();
        handleWire(maxId,script,wire);
        if (i!=0){
            return Result.success("添加成功");
        }else {
            return Result.error(ErrorCodeConstant.DATABASE_OPERATE_ERROR,"添加失败");
        }
    }

    /**
     * 查询待执行线报活动
     * @param
     * @return
     */
    @Override
    public Result queryActivity(Integer pageNo,Integer pageSize) {
        List<WireActivityEntity> wireActivityEntities = wirelistMapper.queryActivity();
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", wireActivityEntities.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        wireActivityEntities = wireActivityEntities.subList(start, end < wireActivityEntities.size() ? end : wireActivityEntities.size());
        result.put("wireActivityList",wireActivityEntities);
        return Result.success(result);
    }

    /**
     * 查询线报活动
     * @param key
     * @return
     */
    @Override
    public Result queryWire(String key,Integer pageNo,Integer pageSize) {
        List<WireEntity> wireEntities = wireMapper.queryWire(key);
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", wireEntities.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        wireEntities = wireEntities.subList(start, end < wireEntities.size() ? end : wireEntities.size());
        result.put("wireList",wireEntities);
        return Result.success(result);
    }

    /**
     * 执行线报
     * @param id
     * @param script
     * @param content
     */
    @Override
    public void handleWire(Integer id, String script, String content) {
        //青龙id->脚本列表
        TreeMap<Integer, List<QlCron>> qlIdToScripts = new TreeMap<>();
        //青龙id->活动id
        TreeMap<Integer, Integer> qlIdToCronId = new TreeMap<>();
        //所有青龙cron状态
        ArrayList<Integer> status = new ArrayList<>();
        //查询所有青龙
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (int i = 0; i < qlEntities.size(); i++) {
            QlEntity qlEntity = qlEntities.get(i);
            List<QlCron> crons = qlUtil.getCrons(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
            qlIdToScripts.put(i,crons);
        }

        //查询所有青龙该任务状态
        for (int i = 0; i < qlIdToScripts.entrySet().size(); i++) {
            List<QlCron> qlCrons = qlIdToScripts.get(i);
            for (QlCron qlCron : qlCrons) {
                if (qlCron.getCommand().contains(script)){
                    qlIdToCronId.put(i,qlCron.getId());
                    status.add(qlCron.getStatus());
                    break;
                }
            }
        }
        //该任务都是未运行中
        if (!status.contains(0)){
            //执行该任务
            handleActivity(id,script,content);
        }
    }


}
