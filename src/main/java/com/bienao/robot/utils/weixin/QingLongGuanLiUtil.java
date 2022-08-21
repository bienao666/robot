package com.bienao.robot.utils.weixin;

import cn.hutool.cache.Cache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class QingLongGuanLiUtil {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private QlUtil qlUtil;

    private Cache<String, String> redis = WXConstant.redis;

    /**
     * 青龙管理
     *
     * @param content
     */
    public void handleQingLong(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            sendQlglMsg(content);
            updateRedisTime(content);
        }else {
            weChatUtil.sendTextMsg("非管理员无法操作",content);
        }
    }

    public void handleOperate(JSONObject content) {
        String msg = content.getString("msg");
        //查询当前操作
        String curOperation = redis.get(content.getString("from_wxid") + "curOperation");
        if (StringUtils.isNotEmpty(curOperation)) {
            handle(curOperation, msg, content);
        } else {
            if (msg.contains("-")) {
                handle("qlgldelete", msg.replace("-", "").trim(), content);
            } else if (msg.contains("删除")) {
                handle("qlgldelete", msg.replace("删除", "").trim(), content);
            } else {
                JSONObject choosees = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "operations"));
                handle(choosees.getString(msg), msg, content);
            }
        }
    }

    public boolean handle(String methed, String param, JSONObject content) {
        String msg = content.getString("msg");
        JSONObject addql = null;
        String addqlStr = "";
        List<JSONObject> qls = new ArrayList<>();
        boolean flag = false;
        try {
            switch (methed) {
                case "qlgldelete":
                    //删除
                    JSONObject qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
                    if (StringUtils.isNotEmpty(param)) {
                        if (NumberUtil.isInteger(param)){
                            qls = JSONArray.parseArray(qlgl.getString("qls"), JSONObject.class);
                            int id = Integer.parseInt(param) - 1;
                            if (id >= 0 && id < qls.size()) {
                                qls.remove(id);
                                qlgl.put("qls", qls);
                                //更新
                                redis.put(content.getString("from_wxid") + "qlgl", qlgl.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                            } else {
                                weChatUtil.sendTextMsg("删除参数有误，请重新输入", content);
                            }
                        }else if ("a".equals(param)){
                            qlgl.put("bigHead","");
                            //更新
                            redis.put(content.getString("from_wxid") + "qlgl", qlgl.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                        }else if ("b".equals(param)){
                            qlgl.put("bigNail","");
                            //更新
                            redis.put(content.getString("from_wxid") + "qlgl", qlgl.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                        }else {
                            weChatUtil.sendTextMsg("删除参数有误，请重新输入", content);
                        }
                    } else {
                        weChatUtil.sendTextMsg("删除参数有误，请重新输入", content);
                    }
                    //更新下一次操作
                    saveQlglOperation(qls, content);
                    sendQlglMsg(content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "qldelete":
                    //删除
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);

                    break;
                case "addQl":
                    //青龙管理-添加
                    //更新ql缓存数据
                    addql = new JSONObject();
                    addql.put("url", "");
                    addql.put("ClientID", "");
                    addql.put("ClientSecret", "");
                    addql.put("limit", "");
                    addql.put("weight", "1");
                    addql.put("smallHead", "");
                    addql.put("smallNail", "");
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送消息
                    weChatUtil.sendTextMsg("请输入青龙面板地址：", content);
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "setQlUrl", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "setQlUrl":
                    //青龙-设置青龙地址
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("url", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    weChatUtil.sendTextMsg("请输入ClientID：", content);
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "setClientID", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "setClientID":
                    //青龙-设置青龙ClientID
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("ClientID", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    weChatUtil.sendTextMsg("请输入ClientSecret：", content);
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "setClientSecret", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "setClientSecret":
                    //青龙-设置青龙ClientSecret
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("ClientSecret", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    weChatUtil.sendTextMsg("请输入备注：", content);
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "setRemark", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "setRemark":
                    //青龙-设置青龙备注
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("remark", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送青龙
                    sendSingleQl(content, addql);
                    //删除当前缓存中的操作
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "goBackSingleQl":
                    //青龙-返回
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
                    qls = JSONArray.parseArray(qlgl.getString("qls"), JSONObject.class);
                    int index = Integer.parseInt(redis.get(content.getString("from_wxid") + "qlIndex"));
                    for (int i = 0; i < qls.size(); i++) {
                        if (i == index) {
                            JSONObject ql = qls.get(i);
                            ql.put("url", addql.getString("url"));
                            ql.put("ClientID", addql.getString("ClientID"));
                            ql.put("ClientSecret", addql.getString("ClientSecret"));
                            ql.put("limit", addql.getString("limit"));
                            ql.put("weight", addql.getString("weight"));
                            ql.put("smallHead", addql.getString("smallHead"));
                            ql.put("smallNail", addql.getString("smallNail"));
                        }
                    }
                    qlgl.put("qls", qls.toString());
                    redis.put(content.getString("from_wxid") + "qlgl", qlgl.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    redis.remove(content.getString("from_wxid") + "qlIndex");
                    redis.remove(content.getString("from_wxid") + "");
                    sendQlglMsg(content);
                    saveQlglOperation(qls, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "exitQl":
                    //青龙-退出
                    redis.remove(content.getString("from_wxid") + "qlgl");
                    redis.remove(content.getString("from_wxid") + "curSession");
                    redis.remove(content.getString("from_wxid") + "addql");
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    redis.remove(content.getString("from_wxid") + "operations");
                    weChatUtil.sendTextMsg("已退出", content);
                    break;
                case "saveQl":
                    //青龙-保存
                    String qlglStr = redis.get(content.getString("from_wxid") + "qlgl");
                    qlgl = JSONObject.parseObject(qlglStr);
                    qls = JSON.parseArray(qlgl.getString("qls"), JSONObject.class);
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    qls.add(addql);
                    qlgl.put("qls", qls);
                    flag = systemParamUtil.updateSystemParam("qlConfig", qlgl.toJSONString());
                    if (flag) {
                        weChatUtil.sendTextMsg("保存成功", content);
                    } else {
                        weChatUtil.sendTextMsg("保存失败", content);
                    }
                    redis.remove(content.getString("from_wxid") + "qlgl");
                    redis.remove(content.getString("from_wxid") + "curSession");
                    redis.remove(content.getString("from_wxid") + "addql");
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    redis.remove(content.getString("from_wxid") + "operations");
                    break;
                case "saveQlgl":
                    //青龙-保存
                    qlglStr = redis.get(content.getString("from_wxid") + "qlgl");
                    flag = systemParamUtil.updateSystemParam("qlConfig", qlglStr);
                    if (flag) {
                        weChatUtil.sendTextMsg("保存成功", content);
                    } else {
                        weChatUtil.sendTextMsg("保存失败", content);
                    }
                    redis.remove(content.getString("from_wxid") + "qlgl");
                    redis.remove(content.getString("from_wxid") + "curSession");
                    redis.remove(content.getString("from_wxid") + "addql");
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    redis.remove(content.getString("from_wxid") + "operations");
                    break;
                case "url":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateUrl", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入青龙面板地址：", content);
                    break;
                case "updateUrl":
                    //青龙-修改青龙地址
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("url", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "ClientID":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateClientID", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入ClientID：", content);
                    break;
                case "updateClientID":
                    //青龙-修改青龙ClientID
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("ClientID", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "ClientSecret":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateClientSecret", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入ClientSecret：", content);
                    break;
                case "updateClientSecret":
                    //青龙-修改青龙ClientSecret
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("ClientSecret", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "limit":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateLimit", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入上限数量：", content);
                    break;
                case "updateLimit":
                    //青龙-修改青龙上限
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("limit", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "weight":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateWeight", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入权重：", content);
                    break;
                case "updateWeight":
                    //青龙-修改青龙权重
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("weight", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "smallHead":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateSmallHead", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入小车头：", content);
                    break;
                case "updateSmallHead":
                    //青龙-修改青龙小车头
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("smallHead", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "preSmallNail":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "updateSmallNail", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入小钉子：", content);
                    break;
                case "smallNail":
                    //青龙-修改青龙小钉子
                    addqlStr = redis.get(content.getString("from_wxid") + "addql");
                    addql = JSONObject.parseObject(addqlStr);
                    addql.put("smallNail", msg);
                    //更新ql缓存数据
                    redis.put(content.getString("from_wxid") + "addql", addql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                    //发送信息
                    sendSingleQl(content, addql);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlOperation(addql, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "queryQl":
                    //青龙管理-查询青龙
                    qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
                    qls = JSON.parseArray(qlgl.getString("qls"), JSONObject.class);
                    if (StringUtils.isNotEmpty(param) && NumberUtil.isInteger(param)) {
                        Integer id = Integer.parseInt(param);
                        if (id > 0 && id <= qls.size()) {
                            JSONObject ql = qls.get(id - 1);
                            //发送信息
                            sendSingleQl(content, ql);
                            //更新下一次操作
                            saveQlOperation(addql, content);
                            //添加缓存
                            redis.put(content.getString("from_wxid") + "qlIndex", String.valueOf(id - 1));
                            redis.put(content.getString("from_wxid") + "addql", ql.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
                        } else {
                            weChatUtil.sendTextMsg("参数有误，请重新输入", content);
                            //更新下一次操作
                            saveQlglOperation(qls, content);
                        }
                    } else {
                        weChatUtil.sendTextMsg("参数有误，请重新输入", content);
                        //更新下一次操作
                        saveQlglOperation(qls, content);
                    }
                    //更新缓存时间
                    updateRedisTime(content);
                    break;
                case "preSetBigHead":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "setBigHead", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入大车头：", content);
                    break;
                case "setBigHead":
                    //青龙管理-设置大车头
                    qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
                    qlgl.put("bigHead", msg);
                    redis.put(content.getString("from_wxid") + "qlgl",qlgl.toJSONString(),DateUnit.SECOND.getMillis() * 60 * 2);
                    qls = JSON.parseArray(qlgl.getString("qls"), JSONObject.class);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlglOperation(qls, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    sendQlglMsg(content);
                    break;
                case "preSetBigNail":
                    //更新下一次操作
                    redis.put(content.getString("from_wxid") + "curOperation", "setBigNail", DateUnit.SECOND.getMillis() * 60 * 2);
                    //更新缓存时间
                    updateRedisTime(content);
                    weChatUtil.sendTextMsg("请输入大钉子：", content);
                    break;
                case "setBigNail":
                    //青龙管理-设置大钉子
                    qlgl = JSONObject.parseObject(redis.get(content.getString("from_wxid") + "qlgl"));
                    qlgl.put("bigNail", msg);
                    redis.put(content.getString("from_wxid") + "qlgl",qlgl.toJSONString(),DateUnit.SECOND.getMillis() * 60 * 2);
                    qls = JSON.parseArray(qlgl.getString("qls"), JSONObject.class);
                    //删除此次缓存
                    redis.remove(content.getString("from_wxid") + "curOperation");
                    //更新下一次操作
                    saveQlglOperation(qls, content);
                    //更新缓存时间
                    updateRedisTime(content);
                    sendQlglMsg(content);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 青龙操作
     *
     * @param ql
     * @param content
     */
    public void saveQlOperation(JSONObject ql, JSONObject content) {
        JSONObject choosees = new JSONObject();
        choosees.put("返回", "goBackSingleQl");
        choosees.put("u", "goBackSingleQl");
        choosees.put("退出", "exitQl");
        choosees.put("q", "exitQl");
        choosees.put("保存", "saveQl");
        choosees.put("wq", "saveQl");
        choosees.put("删除", "qldelete");
        choosees.put("-", "qldelete");
        choosees.put("1", "url");
        choosees.put("2", "ClientID");
        choosees.put("3", "ClientSecret");
        choosees.put("4", "limit");
        choosees.put("5", "weight");
        choosees.put("6", "smallHead");
        choosees.put("7", "smallNail");
        //保存当前操作
        redis.put(content.getString("from_wxid") + "operations", choosees.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
    }

    /**
     * 青龙管理操作
     *
     * @param qls
     * @param content
     */
    public void saveQlglOperation(List<JSONObject> qls, JSONObject content) {
        JSONObject choosees = new JSONObject();
        choosees.put("删除", "qlgldelete");
        choosees.put("-", "qlgldelete");
        choosees.put("增加", "addQl");
        choosees.put("+", "addQl");
        choosees.put("退出", "exitQl");
        choosees.put("q", "exitQl");
        choosees.put("保存", "saveQlgl");
        choosees.put("wq", "saveQlgl");
        for (int i = 0; i < qls.size(); i++) {
            choosees.put(String.valueOf(i + 1), "queryQl");
        }
        choosees.put("a", "preSetBigHead");
        choosees.put("b", "preSetBigNail");
        //保存当前操作
        redis.put(content.getString("from_wxid") + "operations", choosees.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
    }

    /**
     * 推送单个青龙
     */
    public void sendSingleQl(JSONObject content, JSONObject ql) {
        String msg = "请选择对象进行编辑：(返回|u,退出|q,保存|wq)\r\n";
        msg += "1. 面板地址 - " + ql.getString("url") + "\r\n";
        msg += "2. ClientID - " + ql.getString("ClientID") + "\r\n";
        msg += "3. ClientSecret - ******\r\n";
        msg += "4. 上限 - " + ql.getString("limit") + "\r\n";
        msg += "5. 权重 - " + ql.getString("weight") + "\r\n";
        msg += "6. 小车头 - " + ql.getString("smallHead") + "\r\n";
        msg += "7. 小钉子 - " + ql.getString("smallNail");
        weChatUtil.sendTextMsg(msg, content);
    }

    /**
     * 发送青龙管理的内容
     */
    public void sendQlglMsg(JSONObject content) {
        JSONObject qlgl = new JSONObject();
        //查询青龙
        String qlglStr = redis.get(content.getString("from_wxid") + "qlgl");
        if (StringUtils.isEmpty(qlglStr)) {
            qlglStr = systemParamUtil.querySystemParam("qlConfig");
            if (StringUtils.isEmpty(qlglStr)) {
                qlgl.put("qls", new ArrayList<JSONObject>());
                //大车头bigHead
                qlgl.put("bigHead", "");
                //大钉子bigNail
                qlgl.put("bigNail", "");
            } else {
                qlgl = JSONObject.parseObject(qlglStr);
            }
            //添加到缓存中
            redis.put(content.getString("from_wxid") + "qlgl", qlgl.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
        } else {
            qlgl = JSONObject.parseObject(qlglStr);
        }

        String msg = "请选择对象进行编辑：(删除|- 序号，增加|+，退出|q, 保存|wq)\r\n";
        List<JSONObject> qls = JSON.parseArray(qlgl.getString("qls"), JSONObject.class);
        msg += "\r\n青龙列表：\r\n";
        if (qls.size() > 0) {
            for (int i = 0; i < qls.size(); i++) {
                JSONObject ql = qls.get(i);
                msg += (i + 1) + ". " + ql.getString("remark");
                //获取用户秘钥
                JSONObject jsonObject = qlUtil.getToken(ql.getString("url"), ql.getString("ClientID"), ql.getString("ClientSecret"));
                if (jsonObject == null) {
                    msg += "(异常)";
                } else {
                    String token = jsonObject.getString("token");
                    ql.put("token", token);
                    String tokenType = jsonObject.getString("token_type");
                    ql.put("tokenType", tokenType);
                    String expiration = jsonObject.getString("expiration");
                    ql.put("expiration", expiration);
                }
                msg += "\r\n";
            }
        }
        msg += "\r\n配置：\r\n";
        String bigHead = qlgl.getString("bigHead");
        msg += "a.大车头：" + bigHead + "\r\n";
        String bigNail = qlgl.getString("bigNail");
        msg += "b.大钉子：" + bigNail + "\r\n";
        msg += "\r\n警告：2min内未操作则断开此次会话";
        qlgl.put("qls", qls);
        //添加到缓存中
        redis.put(content.getString("from_wxid") + "qlgl", qlgl.toJSONString(), DateUnit.SECOND.getMillis() * 60 * 2);
        saveQlglOperation(qls, content);
        //发送消息
        weChatUtil.sendTextMsg(msg, content);

    }

    public void updateRedisTime(JSONObject content) {
        //更新curSession有效期
        redis.put(content.getString("from_wxid") + "curSession", "青龙管理", DateUnit.SECOND.getMillis() * 60 * 2);
        //更新qlgl有效期
        redis.put(content.getString("from_wxid") + "qlgl", redis.get(content.getString("from_wxid") + "qlgl"), DateUnit.SECOND.getMillis() * 60 * 2);
    }

}
