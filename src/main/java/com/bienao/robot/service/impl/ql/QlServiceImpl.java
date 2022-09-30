package com.bienao.robot.service.impl.ql;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.QlCron;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.QlEnv;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.utils.WxpusherUtil;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class QlServiceImpl implements QlService {

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private QlMapper qlMapper;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WxpusherUtil wxpusherUtil;

    private Cache<String, String> redis = WXConstant.redis;

    private static Pattern ckPattern = Pattern.compile("pt_pin=(.+?);");

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
        if (remark != null && remark.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "remark长度异常");
        }
        String head = ql.getHead();
        if (head != null && head.length() > 20) {
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
            JSONObject tokenJson = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
            if (tokenJson == null) {
                ql.setStatus("异常");
            } else {
                ql.setStatus("正常");
                ql.setToken(tokenJson.getString("token"));
                ql.setTokenType(tokenJson.getString("token_type"));
                qlMapper.updateQl(ql);
                ql.setToken("******");
                ql.setTokenType("******");
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
        if (remark != null && remark.length() > 50) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "remark长度异常");
        }
        String head = ql.getHead();
        if (head != null && head.length() > 20) {
            return Result.error(ErrorCodeConstant.PARAMETER_ERROR, "head长度异常");
        }

        ql.setUpdatedTime(DateUtil.formatDateTime(new Date()));
        if ("******".equals(ql.getClientSecret())) {
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
    @Override
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

        QlEnv qlBigHeadJson = null;
        //查询大车头数据
        for (QlEntity ql : qls) {
            //大车头是否存在
            boolean isContainBigHead = false;
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            for (QlEnv env : envs) {
                try {
                    String name = env.getName();
                    String value = env.getValue();
                    if ("JD_COOKIE".equals(name) && value.contains(qlBigHead)) {
                        //该青龙存在大车头
                        isContainBigHead = true;
                        qlBigHeadJson = env;
                        systemParamUtil.updateSystemParam("BIGHEADLOCATION", "", ql.getId().toString());
                        JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId().toString(), env.getId(), 0);
                        if (jsonObject == null) {
                            results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置失败");
                        } else {
                            results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置成功");
                        }
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            try {
                //大车头是否存在
                boolean isContainBigHead = false;
                List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
                for (QlEnv env : envs) {
                    String name = env.getName();
                    String value = env.getValue();
                    if ("JD_COOKIE".equals(name) && value.contains(qlBigHead)) {
                        //该青龙存在大车头
                        isContainBigHead = true;
                    }
                }
                //该青龙没有大车头
                if (!isContainBigHead) {
                    //设置大车头
                    JSONObject env = qlUtil.addEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), qlBigHeadJson.getName(),
                            qlBigHeadJson.getValue(),
                            qlBigHeadJson.getRemarks());
                    JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getString("id"), env.getInteger("id"), 0);
                    if (jsonObject == null) {
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置失败");
                    } else {
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置成功");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success(results);
    }

    /**
     * 取消车头
     *
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
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            for (QlEnv env : envs) {
                String name = env.getName();
                String value = env.getValue();
                if ("JD_COOKIE".equals(name) && value.contains(qlBigHeadValue) && ql.getId() != Integer.parseInt(bigHeadLocationValue)) {
                    //删除大车头
                    ArrayList<Integer> ids = new ArrayList<>();
                    ids.add(env.getId());
                    boolean b = qlUtil.deleteEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), ids);
                    if (b) {
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "删除车头成功");
                    } else {
                        results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "删除车头失败");
                    }
                }
            }
        }
        return Result.success(results);
    }

    /**
     * 停止脚本
     *
     * @param command
     * @param ids
     * @return
     */
    @Override
    public Result stopScript(String command, List<Integer> ids) {
        ArrayList<String> list = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(ids);
        for (QlEntity ql : qls) {
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
                        String name = cron.getName();
                        if (cron.getCommand().contains(command)) {
                            Integer id = cron.getId();
                            List<Integer> cronIds = new ArrayList<>();
                            cronIds.add(id);
                            boolean flag = qlUtil.stopCron(url, ql.getTokenType(), ql.getToken(), cronIds);
                            if (flag) {
                                list.add(url + "(" + remark + ")" + " 停止" + "(" + name + ")" + " 成功");
                            } else {
                                list.add(url + "(" + remark + ")" + " 停止" + "(" + name + ")" + " 失败");
                            }
                            break;
                        }
                    }
                    Integer now = list.size();
                    if (now.equals(old)) {
                        list.add(url + "(" + remark + ")" + command + " 脚本不存在");
                    }
                } else {
                    list.add(url + "(" + remark + ")" + " 停止" + command + " 失败");
                }

            } catch (Exception e) {
                list.add(url + "(" + remark + ")" + " 停止" + command + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.success(list);
    }

    /**
     * 置顶脚本
     *
     * @param command
     * @param ids
     * @return
     */
    @Override
    public Result pinScript(String command, List<Integer> ids) {
        ArrayList<String> list = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(ids);
        for (QlEntity ql : qls) {
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
                        String name = cron.getName();
                        if (cron.getCommand().contains(command)) {
                            Integer id = cron.getId();
                            List<Integer> cronIds = new ArrayList<>();
                            cronIds.add(id);
                            boolean flag = qlUtil.pinCrons(url, ql.getTokenType(), ql.getToken(), cronIds);
                            if (flag) {
                                list.add(url + "(" + remark + ")" + " 置顶" + "(" + name + ")" + " 成功");
                            } else {
                                list.add(url + "(" + remark + ")" + " 置顶" + "(" + name + ")" + " 失败");
                            }
                            break;
                        }
                    }
                    Integer now = list.size();
                    if (now.equals(old)) {
                        list.add(url + "(" + remark + ")" + command + " 脚本不存在");
                    }
                } else {
                    list.add(url + "(" + remark + ")" + " 置顶" + command + " 失败");
                }

            } catch (Exception e) {
                list.add(url + "(" + remark + ")" + " 置顶" + command + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.success(list);
    }

    /**
     * 禁用脚本
     *
     * @param command
     * @param ids
     * @return
     */
    @Override
    public Result disableScript(String command, List<Integer> ids) {
        ArrayList<String> list = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(ids);
        for (QlEntity ql : qls) {
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
                        String name = cron.getName();
                        if (cron.getCommand().contains(command)) {
                            Integer id = cron.getId();
                            List<Integer> cronIds = new ArrayList<>();
                            cronIds.add(id);
                            boolean flag = qlUtil.disableCrons(url, ql.getTokenType(), ql.getToken(), cronIds);
                            if (flag) {
                                list.add(url + "(" + remark + ")" + " 禁用" + "(" + name + ")" + " 成功");
                            } else {
                                list.add(url + "(" + remark + ")" + " 禁用" + "(" + name + ")" + " 失败");
                            }
                            break;
                        }
                    }
                    Integer now = list.size();
                    if (now.equals(old)) {
                        list.add(url + "(" + remark + ")" + command + " 脚本不存在");
                    }
                } else {
                    list.add(url + "(" + remark + ")" + " 禁用" + command + " 失败");
                }

            } catch (Exception e) {
                list.add(url + "(" + remark + ")" + " 禁用" + command + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.success(list);
    }

    /**
     * 启用脚本
     *
     * @param command
     * @param ids
     * @return
     */
    @Override
    public Result enableScript(String command, List<Integer> ids) {
        ArrayList<String> list = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(ids);
        for (QlEntity ql : qls) {
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
                        String name = cron.getName();
                        if (cron.getCommand().contains(command)) {
                            Integer id = cron.getId();
                            List<Integer> cronIds = new ArrayList<>();
                            cronIds.add(id);
                            boolean flag = qlUtil.enableCrons(url, ql.getTokenType(), ql.getToken(), cronIds);
                            if (flag) {
                                list.add(url + "(" + remark + ")" + " 启用" + "(" + name + ")" + " 成功");
                            } else {
                                list.add(url + "(" + remark + ")" + " 启用" + "(" + name + ")" + " 失败");
                            }
                            break;
                        }
                    }
                    Integer now = list.size();
                    if (now.equals(old)) {
                        list.add(url + "(" + remark + ")" + command + " 脚本不存在");
                    }
                } else {
                    list.add(url + "(" + remark + ")" + " 启用" + command + " 失败");
                }

            } catch (Exception e) {
                list.add(url + "(" + remark + ")" + " 启用" + command + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.success(list);
    }

    /**
     * 查询脚本
     *
     * @return
     */
    @Override
    public Result queryScripts(String key, Integer pageNo, Integer pageSize) {
        HashSet<JSONObject> scriptsSet = new HashSet<>();
        List<JSONObject> scriptsList = new ArrayList<>();
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            try {
                String url = ql.getUrl();
                List<QlCron> crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                if (crons == null) {
                    //重试一次
                    crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                }
                if (crons != null) {
                    for (QlCron cron : crons) {
                        JSONObject script = new JSONObject();
                        String name = cron.getName();
                        script.put("name", name);
                        String command = cron.getCommand().replace("task ", "");
                        script.put("command", command);
                        if (StringUtils.isNotEmpty(key)) {
                            if (name.contains(key) || command.contains(key)) {
                                if (scriptsSet.add(script)) {
                                    scriptsList.add(script);
                                }
                            }
                        } else {
                            if (scriptsSet.add(script)) {
                                scriptsList.add(script);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.error("查询脚本失败", e);
            }
        }
        int start = PageUtil.getStart(pageNo, pageSize) - pageSize;
        int end = PageUtil.getEnd(pageNo, pageSize) - pageSize;
        JSONObject result = new JSONObject();
        result.put("total", scriptsList.size());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        scriptsList = scriptsList.subList(start, end < scriptsList.size() ? end : scriptsList.size());
        result.put("scriptsList", scriptsList);
        return Result.success(result);
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
                List<QlCron> crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                if (crons == null) {
                    //重试一次
                    crons = qlUtil.getCrons(url, ql.getTokenType(), ql.getToken());
                }
                if (crons != null) {
                    Integer old = list.size();
                    for (QlCron cron : crons) {
                        String name = cron.getName();
                        if (cron.getCommand().contains(command)) {
                            Integer id = cron.getId();
                            List<Integer> cronIds = new ArrayList<>();
                            cronIds.add(id);
                            boolean flag = qlUtil.runCron(url, ql.getTokenType(), ql.getToken(), cronIds);
                            if (flag) {
                                list.add(url + "(" + remark + ")" + " 执行" + "(" + name + ")" + " 成功");
                            } else {
                                list.add(url + "(" + remark + ")" + " 执行" + "(" + name + ")" + " 失败");
                            }
                            break;
                        }
                    }
                    Integer now = list.size();
                    if (now.equals(old)) {
                        list.add(url + "(" + remark + ")" + command + " 脚本不存在");
                    }
                } else {
                    list.add(url + "(" + remark + ")" + " 执行" + command + " 失败");
                }

            } catch (Exception e) {
                list.add(url + "(" + remark + ")" + " 执行" + command + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.success(list);
    }

    /**
     * 添加京东ck
     *
     * @return
     */
    @Override
    public void addJdCk(JSONObject content, String ck, String ptPin, String wxPusherUid) {
        //青龙ck数
        ArrayList<Integer> qlCkCountList = new ArrayList<>();
        //查询所有青龙
        List<QlEntity> qls = qlMapper.queryQls(null);
        boolean isReturn = false;

        for (QlEntity ql : qls) {
            if (isReturn) {
                return;
            }
            Integer ckCount = 0;
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            for (QlEnv env : envs) {
                if ("JD_COOKIE".equals(env.getName())) {
                    ckCount++;
                    if (env.getValue().contains(ptPin)) {
                        //更新ck
                        env.setValue(ck);
                        //更新备注
                        String remarks = env.getRemarks();
                        if (StringUtils.isEmpty(remarks)) {
                            env.setRemarks(ptPin + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
                        } else {
                            String[] split = remarks.split("@@");
                            if (split.length == 1) {
                                env.setRemarks(env.getRemarks() + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
                            }
                            if (split.length == 2) {
                                env.setRemarks(split[0] + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
                            }
                            if (split.length == 3) {
                                env.setRemarks(split[0] + "@@" + System.currentTimeMillis() + "@@" + (StringUtils.isEmpty(wxPusherUid) ? split[2] : wxPusherUid));
                            }
                        }
                        if (qlUtil.updateEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), env.getName(), env.getValue(), env.getRemarks())) {
                            sendMessage(content, ptPin, wxPusherUid, ql, "更新");
                        } else {
                            weChatUtil.sendTextMsg("更新失败，请联系管理员", content);
                        }
                        isReturn = true;
                        break;
                    }
                }
            }
            qlCkCountList.add(ckCount);
        }
        //新增
        Integer min = Collections.min(qlCkCountList);
        int i = qlCkCountList.indexOf(min);
        QlEntity ql = qls.get(i);
        JSONObject env = qlUtil.addEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), "JD_COOKIE", ck, ptPin + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
        if (env != null) {
            sendMessage(content, ptPin, wxPusherUid, ql, "添加");
        } else {
            weChatUtil.sendTextMsg("添加失败，请联系管理员", content);
        }
    }

    @Override
    public void setSmallHead() {
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (QlEntity ql : qlEntities) {
            String smallHead = ql.getHead();
            if (StringUtils.isNotEmpty(smallHead)) {
                List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
                for (QlEnv env : envs) {
                    String ck = env.getValue();
                    if (ck.contains(smallHead)) {
                        Integer id = env.getId();
                        if (id != 0) {
                            qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId().toString(), env.getId(), 0);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void sendMessage(JSONObject content, String ptPin, String wxPusherUid, QlEntity ql, String type) {
        //微信推送给用户消息
        weChatUtil.sendTextMsg("robot通知：您已" + type + "成功", content);
        //微信推送给master消息
        weChatUtil.sendTextMsgToMaster("robot通知：用户" + ptPin + "在" + ql.getRemark() + "上" + type + "成功");
        //wxpusher推送用户消息
        try {
            wxpusherUtil.sendLogin(ptPin, wxPusherUid, ql.getRemark());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //wxpusher推送master消息
        String wxpusheruid = systemParamUtil.querySystemParam("WXPUSHERUID");
        if (StringUtils.isNotEmpty(wxpusheruid) && !wxPusherUid.equals(wxpusheruid)) {
            try {
                wxpusherUtil.sendLogin(ptPin, wxpusheruid, ql.getRemark());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
