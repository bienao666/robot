package com.bienao.robot.service.impl.ql;

import cn.hutool.cache.Cache;
import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.QlCron;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.result.Result;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.QingLongGuanLiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Service
@Slf4j
public class QlServiceImpl implements QlService {

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private QingLongGuanLiUtil qingLongGuanLiUtil;

    @Autowired
    private QlMapper qlMapper;

    @Autowired
    private SystemParamUtil systemParamUtil;

    private Cache<String, String> redis = WXConstant.redis;

    /**
     * 一键设置大车头
     *
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
     *
     * @param ql
     */
    @Override
    public Result addQl(QlEntity ql) {
        String url = ql.getUrl();
        if (StringUtils.isEmpty(url)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "地址不能为空");
        }
        if (url.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "地址长度异常");
        }
        String clientID = ql.getClientID();
        if (StringUtils.isEmpty(clientID)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientID不能为空");
        }
        if (clientID.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientID长度异常");
        }
        String clientSecret = ql.getClientSecret();
        if (StringUtils.isEmpty(clientSecret)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientSecret不能为空");
        }
        if (clientSecret.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientSecret长度异常");
        }
        String remark = ql.getRemark();
        if (remark!= null && remark.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "remark长度异常");
        }
        String head = ql.getHead();
        if (head!= null && head.length() > 20) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "head长度异常");
        }

        QlEntity result = qlMapper.queryQlByUrl(url);
        if (result == null) {
            int i = qlMapper.addQl(ql);
            if (i == 1) {
                return Result.success("添加成功");
            } else {
                return Result.error(ErrorCodeConstant.QINGLONG_ADD_ERROR, "青龙添加失败");
            }
        } else {
            return Result.error(ErrorCodeConstant.QINGLONG_EXSIT_ERROR, "青龙面板已存在");
        }
    }

    /**
     * 查询所有青龙
     *
     * @return
     */
    @Override
    public Result queryQls(List<Integer> ids) {
        List<QlEntity> qls = qlMapper.queryQls(ids);
        for (QlEntity ql : qls) {
            JSONObject jsonObject = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
            if (jsonObject == null) {
                ql.setStatus("异常");
            } else {
                ql.setStatus("正常");
            }
            ql.setClientSecret("******");
        }
        return Result.success(qls);
    }

    /**
     * 更新青龙
     *
     * @return
     */
    @Override
    public Result updateQl(QlEntity ql) {
        Integer id = ql.getId();
        if (id == null) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "id不能为空");
        }
        String url = ql.getUrl();
        if (StringUtils.isEmpty(url)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "地址不能为空");
        }
        if (url.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "地址长度异常");
        }
        String clientID = ql.getClientID();
        if (StringUtils.isEmpty(clientID)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientID不能为空");
        }
        if (clientID.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientID长度异常");
        }
        String clientSecret = ql.getClientSecret();
        if (StringUtils.isEmpty(clientSecret)) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientSecret不能为空");
        }
        if (clientSecret.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "clientSecret长度异常");
        }
        String remark = ql.getRemark();
        if (remark!= null && remark.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "remark长度异常");
        }
        String head = ql.getHead();
        if (head!= null && head.length() > 20) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "head长度异常");
        }

        ql.setUpdatedTime(new Date());
        if ("******".equals(ql.getClientSecret())){
            ql.setClientSecret("");
        }
        int i = qlMapper.updateQl(ql);
        if (i == 1) {
            return Result.success("修改成功");
        } else {
            return Result.error(ErrorCodeConstant.QINGLONG_UPDATE_ERROR, "青龙修改失败");
        }
    }

    /**
     * 删除青龙
     *
     * @param ids
     * @return
     */
    public Result deleteQls(List<Integer> ids) {
        int i = qlMapper.deleteQls(ids);
        if (i == 0) {
            return Result.error(ErrorCodeConstant.QINGLONG_DELETE_ERROR, "青龙删除异常");
        } else {
            return Result.success("删除成功");
        }
    }

    /**
     * 一键车头
     *
     * @return
     */
    @Override
    public Result oneKeyHead() {
        ArrayList<String> results = new ArrayList<>();
        //查询大车头
        List<SystemParam> qlbighead = systemParamUtil.querySystemParams("QLBIGHEAD");
        if (qlbighead.size() == 0) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "系统参数不存在");
        }
        SystemParam systemParam = qlbighead.get(0);
        String qlBigHead = systemParam.getValue();
        if (StringUtils.isEmpty(qlBigHead)) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "请先去系统参数设置大车头");
        }

        List<QlEntity> qls = qlMapper.queryQls(null);

        JSONObject qlBigHeadJson = null;
        //查询大车头数据
        for (QlEntity ql : qls) {
            //大车头是否存在
            boolean isContainBigHead = false;
            JSONObject tokenJson = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
            String token = tokenJson.getString("token");
            String tokenType = tokenJson.getString("token_type");
            List<JSONObject> envs = qlUtil.getEnvs(ql.getUrl(), tokenType, token);
            for (JSONObject env : envs) {
                String name = env.getString("name");
                String value = env.getString("value");
                if ("JD_COOKIE".equals(name) && value.contains(qlBigHead)) {
                    //该青龙存在大车头
                    isContainBigHead = true;
                    qlBigHeadJson = env;
                    systemParamUtil.updateSystemParam("BIGHEADLOCATION", "", ql.getId().toString());
                    JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), tokenType, token, env.getString("id"), env.getInteger("id"), 0);
                    if (jsonObject == null){
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置失败");
                    }else {
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置成功");
                    }
                    break;
                }
            }
            if (isContainBigHead) {
                break;
            }
        }

        if (qlBigHeadJson == null) {
            return Result.error(ErrorCodeConstant.BIGHEAD_MOVE_ERROR, "未发现" + qlBigHead + "京东账号");
        }

        //设置大车头
        for (QlEntity ql : qls) {
            //大车头是否存在
            boolean isContainBigHead = false;
            JSONObject tokenJson = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
            String token = tokenJson.getString("token");
            String tokenType = tokenJson.getString("token_type");
            List<JSONObject> envs = qlUtil.getEnvs(ql.getUrl(), tokenType, token);
            for (JSONObject env : envs) {
                String name = env.getString("name");
                String value = env.getString("value");
                if ("JD_COOKIE".equals(name) && value.contains(qlBigHead)) {
                    //该青龙存在大车头
                    isContainBigHead = true;
                }
            }
            //该青龙没有大车头
            if (!isContainBigHead) {
                //设置大车头
                JSONObject env = qlUtil.addEnvs(ql.getUrl(), tokenType, token, qlBigHeadJson.getString("name"),
                        qlBigHeadJson.getString("value"),
                        qlBigHeadJson.getString("remarks"));
                JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), tokenType, token, env.getString("id"), env.getInteger("id"), 0);
                if (jsonObject == null){
                    results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置失败");
                }else {
                    results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置成功");
                }
            }
        }
        return Result.success(results);
    }

    /**
     * 取消车头
     * @return
     */
    @Override
    public Result cancelHead() {
        ArrayList<String> results = new ArrayList<>();
        //查询大车头
        List<SystemParam> systems = systemParamUtil.querySystemParams("QLBIGHEAD");
        if (systems.size() == 0) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "系统参数不存在");
        }
        SystemParam qlBigHead = systems.get(0);
        String qlBigHeadValue = qlBigHead.getValue();
        if (StringUtils.isEmpty(qlBigHeadValue)) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "请先去系统参数设置大车头");
        }
        //查询大车头所属青龙
        systems = systemParamUtil.querySystemParams("BIGHEADLOCATION");
        if (systems.size() == 0) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "系统参数不存在");
        }
        SystemParam bigHeadLocation = systems.get(0);
        String bigHeadLocationValue = bigHeadLocation.getValue();

        List<QlEntity> qls = qlMapper.queryQls(null);

        //查询大车头数据
        for (QlEntity ql : qls) {
            JSONObject tokenJson = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
            String token = tokenJson.getString("token");
            String tokenType = tokenJson.getString("token_type");
            List<JSONObject> envs = qlUtil.getEnvs(ql.getUrl(), tokenType, token);
            for (JSONObject env : envs) {
                String name = env.getString("name");
                String value = env.getString("value");
                if ("JD_COOKIE".equals(name) && value.contains(qlBigHeadValue) && ql.getId()!=Integer.parseInt(bigHeadLocationValue)) {
                    //删除大车头
                    ArrayList<Integer> ids = new ArrayList<>();
                    ids.add(env.getInteger("id"));
                    boolean b = qlUtil.deleteEnvs(ql.getUrl(), tokenType, token, ids);
                    if (b){
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "删除车头成功");
                    }else {
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "删除车头失败");
                    }
                }
            }
        }
        return Result.success(results);
    }

    /**
     * 查询脚本
     *
     * @return
     */
    @Override
    public Result queryScripts(String key,Integer pageNo,Integer pageSize) {
        HashSet<JSONObject> scriptsSet = new HashSet<>();
        List<JSONObject> scriptsList = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            try {
                String url = ql.getUrl();
                String clientId = ql.getClientID();
                String clientSecret = ql.getClientSecret();
                JSONObject tokenJson = qlUtil.getToken(url, clientId, clientSecret);
                String token = tokenJson.getString("token");
                String tokenType = tokenJson.getString("token_type");
                List<QlCron> crons = qlUtil.getCrons(url, tokenType, token);
                if (crons==null){
                    //重试一次
                    crons = qlUtil.getCrons(url, tokenType, token);
                }
                Collections.sort(crons, new Comparator<QlCron>() {
                    @Override
                    public int compare(QlCron o1, QlCron o2) {
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    }
                });
                if (crons != null) {
                    for (QlCron cron : crons) {
                        JSONObject script = new JSONObject();
                        String name = cron.getName();
                        script.put("name", name);
                        String command = cron.getCommand().replace("task ", "");
                        script.put("command", command);
                        if (StringUtils.isNotEmpty(key)) {
                            if (name.contains(key) || command.contains(key)) {
                                if (scriptsSet.add(script)){
                                    scriptsList.add(script);
                                }
                            }
                        } else {
                            if (scriptsSet.add(script)){
                                scriptsList.add(script);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.error("查询脚本失败", e);
            }
        }
        int start = PageUtil.getStart(pageNo, pageSize);
        int end = PageUtil.getEnd(pageNo, pageSize);
        scriptsList = scriptsList.subList(start,end);
        JSONObject result = new JSONObject();
        result.put("total",scriptsList.size());
        result.put("pageNo",pageNo);
        result.put("pageSize",pageSize);
        result.put("scriptsList",scriptsList);
        return Result.success(result    );
    }

    /**
     * 执行脚本
     *
     * @param command
     * @return
     */
    @Override
    public Result runScript(String command, List<Integer> ids) {
        ArrayList<String> list = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(ids);
        for (QlEntity ql : qls) {
            String remark = ql.getRemark();
            String url = ql.getUrl();
            try {
                JSONObject tokenJson = qlUtil.getToken(url, ql.getClientID(), ql.getClientSecret());
                String token = tokenJson.getString("token");
                String tokenType = tokenJson.getString("token_type");
                List<QlCron> crons = qlUtil.getCrons(url, tokenType, token);
                if (crons==null){
                    //重试一次
                    crons = qlUtil.getCrons(url, tokenType, token);
                }
                if (crons!=null){
                    Integer old = list.size();
                    for (QlCron cron : crons) {
                        String name = cron.getName();
                        if (cron.getCommand().contains(command)) {
                            Integer id = cron.getId();
                            List<Integer> cronIds = new ArrayList<>();
                            cronIds.add(id);
                            boolean flag = qlUtil.runCron(url, tokenType, token, cronIds);
                            if (flag) {
                                list.add(url + "(" + remark + ")" + " 执行" + "(" + name + ")" + " 成功");
                            } else {
                                list.add(url + "(" + remark + ")" + " 执行" + "(" + name + ")" + " 失败");
                            }
                            break;
                        }
                    }
                    Integer now = list.size();
                    if (now == old) {
                        list.add(url + "(" + remark + ")" + command + " 脚本不存在");
                    }
                }else {
                    list.add(url + "(" + remark + ")" + " 执行" + command + " 失败");
                }

            } catch (Exception e) {
                list.add(url + "(" + remark + ")" + " 执行" + command + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.success(list);
    }
}
