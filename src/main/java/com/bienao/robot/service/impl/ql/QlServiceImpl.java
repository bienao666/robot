package com.bienao.robot.service.impl.ql;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.PatternConstant;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.QlCron;
import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.QlEnv;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.QlMapper;
import com.bienao.robot.entity.Result;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.utils.WxpusherUtil;
import com.bienao.robot.utils.jingdong.JDUtil;
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

    @Autowired
    private JdCkMapper jdCkMapper;

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
            try {
                JSONObject tokenJson = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
                if (tokenJson == null) {
                    ql.setStatus("异常");
                    weChatUtil.sendTextMsgToMaster(ql.getRemark() + "青龙服务器状态异常");
                } else {
                    ql.setStatus("正常");
                    ql.setToken(tokenJson.getString("token"));
                    ql.setTokenType(tokenJson.getString("token_type"));
                    qlMapper.updateQl(ql);
                    //获取当前青龙的京东ck
                    List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
                    String first = "";
                    Integer validCount = 0;
                    Integer invalidCount = 0;
                    Integer count = 0;
                    for (QlEnv env : envs) {
                        if ("JD_COOKIE".equals(env.getName())) {
                            count++;
                            if (env.getStatus() == 0) {
                                validCount++;
                            }
                            if (env.getStatus() == 1) {
                                invalidCount++;
                            }
                            if (StringUtils.isEmpty(first)) {
                                Matcher matcher = PatternConstant.ckPattern.matcher(env.getValue());
                                if (matcher.find()) {
                                    first = matcher.group(2);
                                }
                            }
                        }
                    }
                    ql.setCurFirst(first);
                    ql.setValidCount(validCount);
                    ql.setInvalidCount(invalidCount);
                    ql.setCount(count);
                    ql.setToken("******");
                    ql.setTokenType("******");
                }
                ql.setClientSecret("******");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success(qls);
    }

    /**
     * 青龙检测
     */
    @Override
    public Result checkQl() {
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            try {
                JSONObject tokenJson = qlUtil.getToken(ql.getUrl(), ql.getClientID(), ql.getClientSecret());
                if (tokenJson == null) {
                    ql.setStatus("异常");
                    weChatUtil.sendTextMsgToMaster(ql.getRemark() + "青龙服务器状态异常");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        String qlBigHead = systemParamUtil.querySystemParam("QLBIGHEAD");
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
                        JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), env.getId(), 0);
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
                        JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), 1000, 0);
                        if (jsonObject == null) {
                            results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置失败");
                        } else {
                            results.add(ql.getUrl() + "(" + ql.getRemark() + ")" + "设置成功");
                        }
                    }
                }
                //该青龙没有大车头
                if (!isContainBigHead) {
                    //设置大车头
                    QlEnv env = qlUtil.addEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), qlBigHeadJson.getName(),
                            qlBigHeadJson.getValue(),
                            qlBigHeadJson.getRemarks());
                    JSONObject jsonObject = qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), 1000, 0);
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
        String qlBigHeadValue = systemParamUtil.querySystemParam("QLBIGHEAD");
        if (StringUtils.isEmpty(qlBigHeadValue)) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "请先去系统参数设置大车头");
        }
        //查询大车头所属青龙
        String bigHeadLocation = systemParamUtil.querySystemParam("BIGHEADLOCATION");
        if (StringUtils.isEmpty(bigHeadLocation)) {
            return Result.error(ErrorCodeConstant.SYSTEMPARAM_ERROR, "系统参数不存在");
        }

        List<QlEntity> qls = qlMapper.queryQls(null);

        //查询大车头数据
        for (QlEntity ql : qls) {
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            for (QlEnv env : envs) {
                String name = env.getName();
                String value = env.getValue();
                if ("JD_COOKIE".equals(name) && value.contains(qlBigHeadValue) && ql.getId() != Integer.parseInt(bigHeadLocation)) {
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
                            ArrayList<Integer> ids = new ArrayList<>();
                            ids.add(env.getId());
                            qlUtil.enableEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), ids);
                            qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), 1000, 6);
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
        if (!isReturn) {
            //新增
            Integer min = Collections.min(qlCkCountList);
            int i = qlCkCountList.indexOf(min);
            QlEntity ql = qls.get(i);
            QlEnv env = qlUtil.addEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), "JD_COOKIE", ck, ptPin + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
            if (env != null) {
                sendMessage(content, ptPin, wxPusherUid, ql, "添加");
                qlUtil.moveEnv(ql.getUrl(),ql.getTokenType(),ql.getToken(),env.getId(),1000,6);
            } else {
                weChatUtil.sendTextMsg("添加失败，请联系管理员", content);
            }
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
                        qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), 1000, 0);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 韭菜友好设置
     */
    @Override
    public void leekFriendly() {
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (QlEntity ql : qlEntities) {
            log.info("青龙服务器：{}开始韭菜友好排序。。。", ql.getRemark());
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            //保留前五个ck不动
            int limit = 5;
            int count = 0;
            //第五个ck的索引
            int index = 0;
            //存放五个以后的ck
            TreeMap<Integer, QlEnv> treeMap = new TreeMap<>();
            for (int i = 0; i < envs.size(); i++) {
                QlEnv env = envs.get(i);
                if (!"JD_COOKIE".equals(env.getName())) {
                    continue;
                }
                count++;
                if (count <= limit) {
                    index = i;
                    continue;
                }
                //查询前一天的京豆收益
                JdCkEntity jdCkEntityQuery = new JdCkEntity();
                jdCkEntityQuery.setCk(env.getValue());
                JdCkEntity jdCkEntity = jdCkMapper.queryCk(jdCkEntityQuery);
                if (jdCkEntity != null) {
                    Integer jd = jdCkEntity.getJd();
                    if (jd > 10) {
                        treeMap.put(jd, env);
                    }
                }
            }
            for (Map.Entry<Integer, QlEnv> entry : treeMap.entrySet()) {
                QlEnv qlEnv = entry.getValue();
                qlUtil.moveEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), qlEnv.getId(), 1000, ++index);
            }
        }
    }

    /**
     * 多青龙 ck分布优化
     */
    @Override
    public void autoAdjust() {
        HashMap<Integer, Integer> qlToCkCount = new HashMap<>();
        HashMap<Integer, List<QlEnv>> qlToQlEnvs = new HashMap<>();
        HashMap<String, QlEnv> ptPinToQlEnv = new HashMap<>();
        //青龙列表
        List<QlEntity> qlEntities = qlMapper.queryQls(null);
        for (int i = 0; i < qlEntities.size(); i++) {
            QlEntity qlEntity = qlEntities.get(i);
            try {
                List<QlEnv> envs = qlUtil.getEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
                //获取当前有效ck数量
                for (int j = 0; j < envs.size(); j++) {
                    QlEnv qlEnv = envs.get(j);
                    if (qlEnv.getName().equals("JD_COOKIE")) {
                        qlEnv.setQlId(qlEntity.getId());
                        qlEnv.setQlIndex(j);
                        Matcher matcher = PatternConstant.jdPinPattern.matcher(qlEnv.getValue());
                        if (matcher.find()) {
                            String jdPin = matcher.group(1);
                            QlEnv qlEnv1 = ptPinToQlEnv.get(jdPin);
                            if (qlEnv1 == null) {
                                //未出现过
                                ptPinToQlEnv.put(jdPin, qlEnv);
                                countQlCkCounts(qlToCkCount, qlEnv.getQlId(), 1);
                                operateQlToQlEnvs(qlToQlEnvs, qlEnv.getQlId(), qlEnv, 0);
                            } else {
                                //出现过，两个重复ck
                                if (qlEnv.getValue().equals(qlEnv1.getValue())) {
                                    //ck相同，删一个索引靠后的
                                    if (qlEnv.getQlIndex() < qlEnv1.getQlIndex()) {
                                        //删除qlEnv1
                                        deleteQlCk(qlMapper.queryQl(qlEnv1.getQlId()), qlEnv1);
                                        //添加qlEnv
                                        ptPinToQlEnv.put(jdPin, qlEnv);
                                        //qlEnv1所在青龙的ck个数-1
                                        countQlCkCounts(qlToCkCount, qlEnv1.getQlId(), -1);
                                        //qlEnv所在青龙的ck个数+1
                                        countQlCkCounts(qlToCkCount, qlEnv.getQlId(), 1);
                                        //qlEnv1所在青龙减去
                                        operateQlToQlEnvs(qlToQlEnvs, qlEnv1.getQlId(), qlEnv1, 1);
                                        //qlEnv所在青龙加上
                                        operateQlToQlEnvs(qlToQlEnvs, qlEnv.getQlId(), qlEnv, 0);
                                    } else {
                                        //删除qlEnv
                                        deleteQlCk(qlEntity, qlEnv);
                                    }
                                } else {
                                    //ck不相同，有一个无效，删一个无效的索引
                                    boolean isValid = JDUtil.isVaild(qlEnv.getValue());
                                    boolean isValid1 = JDUtil.isVaild(qlEnv1.getValue());
                                    if (isValid && isValid1) {
                                        //两个都有效，删除老数据
                                        if (DateUtil.parse(qlEnv.getCreatedAt()).getTime() < DateUtil.parse(qlEnv1.getCreatedAt()).getTime()) {
                                            //删除qlEnv
                                            deleteQlCk(qlEntity, qlEnv);
                                        } else {
                                            //删除qlEnv1
                                            deleteQlCk(qlMapper.queryQl(qlEnv1.getQlId()), qlEnv1);
                                            //添加qlEnv
                                            ptPinToQlEnv.put(jdPin, qlEnv);
                                            //qlEnv1所在青龙的ck个数-1
                                            countQlCkCounts(qlToCkCount, qlEnv1.getQlId(), -1);
                                            //qlEnv所在青龙的ck个数+1
                                            countQlCkCounts(qlToCkCount, qlEnv.getQlId(), 1);
                                            //qlEnv1所在青龙减去
                                            operateQlToQlEnvs(qlToQlEnvs, qlEnv1.getQlId(), qlEnv1, 1);
                                            //qlEnv所在青龙加上
                                            operateQlToQlEnvs(qlToQlEnvs, qlEnv.getQlId(), qlEnv, 0);
                                        }
                                    } else {
                                        if (isValid || isValid1) {
                                            //一个无效，一个有效，删除无效
                                            if (!isValid) {
                                                //删除qlEnv
                                                deleteQlCk(qlEntity, qlEnv);
                                            }
                                            if (!isValid1) {
                                                //删除qlEnv1
                                                deleteQlCk(qlMapper.queryQl(qlEnv1.getQlId()), qlEnv1);
                                                //添加qlEnv
                                                ptPinToQlEnv.put(jdPin, qlEnv);
                                                //qlEnv1所在青龙的ck个数-1
                                                countQlCkCounts(qlToCkCount, qlEnv1.getQlId(), -1);
                                                //qlEnv所在青龙的ck个数+1
                                                countQlCkCounts(qlToCkCount, qlEnv.getQlId(), 1);
                                                //qlEnv1所在青龙减去
                                                operateQlToQlEnvs(qlToQlEnvs, qlEnv1.getQlId(), qlEnv1, 1);
                                                //qlEnv所在青龙加上
                                                operateQlToQlEnvs(qlToQlEnvs, qlEnv.getQlId(), qlEnv, 0);
                                            }
                                        } else {
                                            //两个都无效，删除qlEnv
                                            deleteQlCk(qlEntity, qlEnv);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //根据每个青龙传ck个数，进行优化
        //青龙总数
        Integer qlCount = qlToCkCount.size();
        //ck总数
        Integer cks = 0;
        for (Map.Entry<Integer, Integer> entry : qlToCkCount.entrySet()) {
            cks += entry.getValue();
        }
        //ck平均值
        Integer average = cks / qlCount + 1;
        //ck转移
        for (Map.Entry<Integer, Integer> count : qlToCkCount.entrySet()) {
            if (count.getValue() > average) {
                //ck数大于平均值
                for (Map.Entry<Integer, Integer> count1 : qlToCkCount.entrySet()) {
                    if (count1.getValue() < average) {
                        //往ck数小于平均值的青龙上转
                        if (!count.getKey().equals(count1.getKey())) {
                            try {
                                QlEntity qlEntity = qlMapper.queryQl(count.getKey());
                                QlEntity qlEntity1 = qlMapper.queryQl(count1.getKey());
                                List<QlEnv> qlEnvs = qlToQlEnvs.get(count.getKey());
                                List<QlEnv> qlEnvs1 = qlToQlEnvs.get(count1.getKey());
                                for (int i = average + 1; i < qlEnvs.size(); i++) {
                                    if (count1.getValue() > average) {
                                        break;
                                    }
                                    QlEnv qlEnv = qlEnvs.get(i);
                                    QlEnv res = qlUtil.addEnvs(qlEntity1.getUrl(), qlEntity1.getTokenType(), qlEntity1.getToken(), qlEnv.getName(), qlEnv.getValue(), qlEnv.getRemarks());
                                    if (res != null) {
                                        //添加成功
                                        qlEnvs1.add(qlEnv);
                                        count1.setValue(count1.getValue() + 1);
                                        //原青龙上删除数据
                                        deleteQlCk(qlEntity, qlEnv);
                                        qlEnvs.remove(qlEnv);
                                        count.setValue(count.getValue() - 1);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void checkCk() {
        List<QlEntity> qls = qlMapper.queryQls(null);
        for (QlEntity ql : qls) {
            List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
            ArrayList<Integer> enableList = new ArrayList<>();
            ArrayList<Integer> disableList = new ArrayList<>();
            for (QlEnv env : envs) {
                if ("JD_COOKIE".equals(env.getName())) {
                    if (JDUtil.isVaild(env.getValue())) {
                        //有效
                        if (env.getStatus() == 1) {
                            enableList.add(env.getId());
                        }
                    } else {
                        //无效
                        if (env.getStatus() == 0) {
                            disableList.add(env.getId());
                        }
                    }
                    //休息5s防止黑ip
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (enableList.size() > 0) {
                qlUtil.enableEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), enableList);
            }
            if (disableList.size() > 0) {
                qlUtil.disableEnv(ql.getUrl(), ql.getTokenType(), ql.getToken(), disableList);
            }
        }
    }

    private void countQlCkCounts(HashMap<Integer, Integer> qlToCkCount, Integer qlID, Integer value) {
        Integer count = qlToCkCount.get(qlID);
        if (count == null) {
            qlToCkCount.put(qlID, 1);
        } else {
            qlToCkCount.put(qlID, count + value);
        }
    }

    /**
     * @param qlToQlEnvs
     * @param qlID
     * @param qlEnv
     * @param operate    0增加 1删除
     */
    private void operateQlToQlEnvs(HashMap<Integer, List<QlEnv>> qlToQlEnvs, Integer qlID, QlEnv qlEnv, Integer operate) {
        List<QlEnv> qlEnvs = qlToQlEnvs.get(qlID);
        if (qlEnvs == null) {
            qlEnvs = new ArrayList<>();
        }
        if (operate == 0) {
            qlEnvs.add(qlEnv);
        } else {
            qlEnvs.remove(qlEnv);
        }
        qlToQlEnvs.put(qlID, qlEnvs);
    }

    private void deleteQlCk(QlEntity qlEntity, QlEnv qlEnv) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(qlEnv.getId());
        qlUtil.deleteEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken(), ids);
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
