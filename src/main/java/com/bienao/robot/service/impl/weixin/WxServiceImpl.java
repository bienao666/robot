package com.bienao.robot.service.impl.weixin;

import cn.hutool.cache.Cache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.FunctionType;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.*;
import com.bienao.robot.entity.jingdong.JdCkEntity;
import com.bienao.robot.mapper.*;
import com.bienao.robot.mapper.jingdong.JdCkMapper;
import com.bienao.robot.service.jingdong.CkService;
import com.bienao.robot.service.jingdong.JdService;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.service.user.UserService;
import com.bienao.robot.service.weixin.ForwardService;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.*;
import com.bienao.robot.utils.jingdong.JdBeanChangeUtil;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import com.google.common.collect.EvictingQueue;
import com.nlf.calendar.Lunar;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WxServiceImpl implements WxService {

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private QlService qlService;

    @Autowired
    private HeFengWeatherUtil heFengWeatherUtil;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private YlgyMapper ylgyMapper;

    @Autowired
    private WxbsMapper wxbsMapper;

    @Autowired
    private WxpusherUtil wxpusherUtil;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JdCkMapper jdCkMapper;

    @Autowired
    private CkService ckService;

    @Autowired
    private JdBeanChangeUtil jdBeanChangeUtil;

    @Autowired
    private QlMapper qlMapper;

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private ForwardMapper forwardMapper;

    @Autowired
    private ForwardService forwardService;

    @Autowired
    private ForwardUtil forwardUtil;

    @Autowired
    private WireService wireService;

    private Cache<String, String> redis = WXConstant.redis;

    private Pattern lowerDatePattern = Pattern.compile("/Date\\((\\d+)\\+");

    private static Pattern jdPinPattern = Pattern.compile("pt_pin=(.+?);");

    private static Pattern ckPattern = Pattern.compile("pt_key=(.+?);pt_pin=(.+?);");

    @Async("asyncServiceExecutor")
    @Override
    public void handleMessage(JSONObject message) {
        JSONObject content = message.getJSONObject("content");

        //同意好友添加
        Integer type = content.getInteger("type");
        if (14 == type) {
            String isautoagreeaddwx = systemParamUtil.querySystemParam("ISAUTOAGREEADDWX");
            if ("是".equals(isautoagreeaddwx)){
                weChatUtil.agreeFriendVerify(content);
            }
            return;
        }

        //获取微信群号
        String from_group = content.getString("from_group");

        //记录群
        if (StringUtils.isNotEmpty(from_group)) {
            List<Group> groups = groupMapper.queryGroupByGroupId(from_group);
            if (groups.size() == 0) {
                Group group = new Group();
                group.setGroupid(from_group);
                group.setGroupName(content.getString("from_group_name"));
                groupMapper.addGroup(group);
            }
        }

        //判断用户是否白名单
        if (!checkUserStatus(content)){
            return;
        }

        //发送人
        String from_wxid = content.getString("from_wxid");


        String msg = content.getString("msg").trim();

        //转发
        List<ForwardEntity> list = forwardMapper.queryForward(String.valueOf(from_group), null, null, null);
        if (list.size() > 0) {
            for (ForwardEntity forwardEntity : list) {
                forwardUtil.forward(msg, forwardEntity.getTo(), forwardEntity.getTotype());
            }
        }


        //机器人
        String robortwxid = systemParamUtil.querySystemParam("ROBORTWXID");
        if (StringUtils.isEmpty(robortwxid)) {
            systemParamUtil.updateSystemParam("ROBORTWXID", "机器人", content.getString("robot_wxid"));
        }

        //监听群
        if ("监听".equals(msg.trim()) && StringUtils.isNotEmpty(from_group) && weChatUtil.isMaster(content)) {
            Group group = groupMapper.queryGroupByGroupIdAndFunctionType(from_group, FunctionType.wxqjk);
            if (group == null) {
                group = new Group();
                group.setGroupid(from_group);
                group.setGroupName(content.getString("from_group_name"));
                group.setFunctionType(FunctionType.wxqjk);
                int i = groupMapper.addGroup(group);
                if (i == 0) {
                    weChatUtil.sendTextMsg("监听失败", content);
                } else {
                    weChatUtil.sendTextMsg("开始监听", content);
                    redis.remove("validGroups");
                }
            } else {
                weChatUtil.sendTextMsg("开始监听", content);
            }
            return;
        }

        //取消监听群
        if ("取消监听".equals(msg.trim()) && StringUtils.isNotEmpty(from_group) && weChatUtil.isMaster(content)) {
            int i = groupMapper.deleteGroupByGroupIdAndFunctionType(from_group, FunctionType.wxqjk);
            if (i > 0) {
                weChatUtil.sendTextMsg("取消监听成功", content);
                redis.remove("validGroups");
            } else {
                weChatUtil.sendTextMsg("未监听此群，无法取消", content);
            }
            return;
        }
        //判断是否在监听范围
        if (StringUtils.isNotEmpty(from_group)) {
            //获取所有监听群号
            String validGroups = redis.get("validGroups");
            if (StringUtils.isEmpty(validGroups)) {
                List<Group> groups = groupMapper.queryGroupByFunctionType(FunctionType.wxqjk);
                validGroups = StringUtils.join(groups, "#");
                redis.put("validGroups", validGroups);
            }
            if (validGroups == null || !validGroups.contains(from_group)) {
                //此群不在监听范围内
                return;
            }
        }

        if (msg.contains("export ")) {
            String islistenwire = systemParamUtil.querySystemParam("ISLISTENWIRE");
            if ("是".equals(islistenwire)){
                Result result = wireService.addActivity(msg);
                if ("200".equals(result.getCode())) {
                    weChatUtil.sendTextMsg("线报添加成功", content);
                } else {
                    weChatUtil.sendTextMsg(result.getMessage(), content);
                }
            }
            return;
        }

        //京东ck
        if (ckPattern.matcher(msg.replace(" ","").trim()).find()){
            addJdCk(msg,content);
            return;
        }

        //退出当前操作
        if ("q".equals(msg)) {
            log.info("退出当前操作：{}", msg);
            redis.remove(from_wxid + "operate");
            weChatUtil.sendTextMsg("已退出", content);
            return;
        }

        //查看当前操作
        String operate = redis.get(from_wxid + "operate");
        if (StringUtils.isNotEmpty(operate)) {
            log.info("处理当前操作：{}", operate);
            handleOperate(content, operate, msg, from_wxid);
            return;
        }

        //系统参数
        if (msg.startsWith("设置") || msg.startsWith("关闭")) {
            handleSetSysParam(content);
            return;
        }
        //扭一扭
        if ("扭".equals(msg) || "扭一扭".equals(msg)){
            handleNiuYiNiu(content);
            return;
        }
        //功能列表
        /*if ("菜单".equals(msg)) {
            handleFunctionList(content);
            return;
        }*/
        //转发
        if (msg.startsWith("转发")) {
            handleForward(content);
            return;
        }
        //取消转发
        if (msg.startsWith("取消转发")) {
            handleQxForward(content);
            return;
        }
        //京东资产查询
        if ("查询".equals(msg)) {
            handleQueryJdAssets(content);
            return;
        }
        //羊了个羊
        if (msg.startsWith("羊 ")) {
            handleYLGY(content);
            return;
        }
        //羊了个羊
        if (msg.startsWith("羊t ")) {
            handleYLGYt(content);
            return;
        }
        //博客
        if ("博客".equals(msg)) {
            handleFunctionBoKe(content);
            return;
        }
        //加群
        if ("加群".equals(msg) || "进群".equals(msg)) {
            handleAddGroup(content);
            return;
        }
        //禁用
        if (msg.startsWith("禁用")) {
            handleDisableCk(content);
            return;
        }
        //启用
        if (msg.startsWith("启用")) {
            handleEnableCk(content);
            return;
        }

        //登陆
        if ("登陆".equals(msg) || "登录".equals(msg)) {
            handleJdLogin(content);
            return;
        }
        //饿了么
        if ("饿了么".equals(msg.trim()) || "elm".equals(msg.trim())) {
            handleELM(content);
            return;
        }
        //支付宝红包
        if (msg.trim().equals("支付宝红包") || msg.trim().equals("zfb")) {
            handleZfbHb(content);
            return;
        }
        //微博
        if ("微博".equals(msg.trim()) || "wb".equals(msg.trim())) {
            handleWeiBo(content);
            return;
        }
        //监控茅台洋河
        if ("监控茅台洋河".equals(msg.trim())) {
            handleJkMtYh(content);
            return;
        }
        //取消茅台洋河监控
        if ("取消监控茅台洋河".equals(msg.trim())) {
            handleQxMtYhJk(content);
            return;
        }
        //饿了么推送
        if ("推送饿了么".equals(msg.trim())) {
            handleElmTs(content);
            return;
        }
        //取消饿了么推送
        if ("取消推送饿了么".equals(msg.trim())) {
            handleQxElmTs(content);
            return;
        }
        //推送摸鱼
        if ("推送摸鱼".equals(msg.trim())) {
            handleTsMy(content);
            return;
        }
        //取消推送摸鱼
        if ("取消推送摸鱼".equals(msg.trim())) {
            handleQxTsMy(content);
            return;
        }
        //推送微博
        if ("推送微博".equals(msg.trim())) {
            handleTsWb(content);
            return;
        }
        //取消推送微博
        if ("取消推送微博".equals(msg.trim())) {
            handleQxTsWb(content);
            return;
        }
        //命令
        if ("命令".equals(msg)) {
            handleCommand(content);
            return;
        }
        //推送支付宝红包
        if (msg.trim().equals("推送支付宝红包")) {
            handleTsZfbHb(content);
            return;
        }
        //取消推送支付宝红包
        if (msg.trim().equals("取消推送支付宝红包")) {
            handleQxTsZfbHb(content);
            return;
        }
        //举牌
        if (msg.contains("举牌")) {
            handleJuPai(content);
            return;
        }
        //油价
        if (msg.endsWith("油价")) {
            handleYouJia(content);
            return;
        }
        //买家秀
        if ("mjx".equals(msg.trim()) || "买家秀".equals(msg.trim())) {
            handleMJX(content);
            return;
        }
        //老色批
        if ("lsp".equals(msg.trim()) || "老色批".equals(msg.trim())) {
            handleLSP(content);
            return;
        }
        //摸鱼
        if ("my".equals(msg.trim()) || "摸鱼".equals(msg.trim())) {
            handleMoYu(content);
            return;
        }
        //天气
        if (msg.trim().contains("天气")) {
            handleWeather(content);
            return;
        }
        if ("是".equals(systemParamUtil.querySystemParam("ISCOMPAREGOODSPRICE"))){
            //比价
            if ("比价".equals(msg)) {
                weChatUtil.sendTextMsg("请直接发送商品连接，我会自动识别", content);
                return;
            }
            if (msg.contains("item.m.jd.com") || msg.contains("m.tb.cn") || msg.contains("mobile.yangkeduo.com")) {
                handleGoods(msg, content);
                return;
            }
        }

        //查询我的uid
        if ("我的uid".equals(msg.trim()) || "myuid".equals(msg.trim())) {
            handleMyUid(content);
            return;
        }
        //查询所有群号
        if ("群号列表".equals(msg.trim())) {
            handleGroupCodes(content);
            return;
        }
        //查询群id
        if (msg.startsWith("群号")) {
            handleGroupCode(content);
            return;
        }
        //查询微信管理员
        if ("微信管理员列表".equals(msg.trim())) {
            handleQueryWXMasters(content);
            return;
        }

        List<CommandEntity> commandEntities = commandMapper.queryCommand("", "",0);
        for (CommandEntity commandEntity : commandEntities) {
            if (msg.trim().equals(commandEntity.getCommand())) {
                weChatUtil.sendTextMsg(commandEntity.getReply(), content);
                return;
            }
        }

        if (NumberUtil.isInteger(msg)) {
            Integer num = Integer.valueOf(msg);
            String publicKey = redis.get("publicKey");
            if (num <= 50 && StringUtils.isNotEmpty(publicKey)) {
                handleLast(content, num, publicKey);
            }
        }
    }

    /**
     * 扭一扭
     * @param content
     */
    private void handleNiuYiNiu(JSONObject content) {
        String url = "http://api.qemao.com/api/douyin/";
        url += HttpRequest.get(url).execute().header("location").replace("./","");
        weChatUtil.sendVideoMsg(url,content);
    }

    public boolean checkUserStatus(JSONObject content){
        String from_wxid = content.getString("from_wxid");
        User userQuery = new User();
        userQuery.setWxid(from_wxid);
        User user = userService.queryUser(userQuery);
        if (user==null){
            //新增
            user = new User();
            user.setWxid(from_wxid);
            user.setWxName(content.getString("from_name"));
            userService.addUser(user);
        }else {
            if (user.getStatus()==1){
                //已拉黑
                log.info(from_wxid+"该用户已拉黑");
                return false;
            }
        }
        log.info(from_wxid+"该用户正常");
        return true;
    }

    /**
     * 取消转发群消息
     * @param content
     */
    private void handleQxForward(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag){
            String msg = content.getString("msg").replace("取消转发", "").trim();
            String[] split = msg.split(" ");
            if (split.length != 2){
                weChatUtil.sendTextMsg("取消转发格式有误",content);
                return;
            }
            String fromGroupId = split[0];
            String toGroupId = split[1];
            List<ForwardEntity> forwardEntities = forwardMapper.queryForward(fromGroupId, null, toGroupId, null);
            if (forwardEntities.size() == 0 ){
                weChatUtil.sendTextMsg("转发不存在",content);
                return;
            }
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(forwardEntities.get(0).getId());
            int i = forwardService.deleteForward(ids);
            if (i == 0){
                weChatUtil.sendTextMsg("取消失败",content);
            }else {
                weChatUtil.sendTextMsg("取消转发",content);
            }
        }
    }

    /**
     * 转发群消息
     * @param content
     */
    private void handleForward(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag){
            String msg = content.getString("msg").replace("转发", "").trim();
            String[] split = msg.split(" ");
            if (split.length != 2){
                weChatUtil.sendTextMsg("转发格式有误",content);
                return;
            }
            String fromGroupId = split[0];
            String toGroupId = split[1];
            List<ForwardEntity> forwardEntities = forwardMapper.queryForward(fromGroupId, null, toGroupId, null);
            if (forwardEntities.size() > 0 ){
                weChatUtil.sendTextMsg("改转发已设置过",content);
                return;
            }
            List<Group> groups = groupMapper.queryDistinctGroup(null, fromGroupId);
            if (groups.size()!=1){
                weChatUtil.sendTextMsg(fromGroupId+"群不存在",content);
                return;
            }
            Group fromGroup = groups.get(0);
            groups = groupMapper.queryDistinctGroup(null, toGroupId);
            if (groups.size()!=1){
                weChatUtil.sendTextMsg(fromGroupId+"群不存在",content);
                return;
            }
            Group toGroup = groups.get(0);
            int i = forwardService.addForward(fromGroup,toGroup);
            if (i == 1){
                weChatUtil.sendTextMsg("开启转发",content);
            }else {
                weChatUtil.sendTextMsg("转发失败",content);
            }
        }
    }

    /**
     * 查询所有群号列表
     * @param content
     */
    private void handleGroupCodes(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag){
            List<Group> groups = groupMapper.queryDistinctGroup(null,null);
            String msg = "";
            for (Group group : groups) {
                msg += group.getGroupName() + "\n  " + group.getGroupid() + "\n";
            }
            weChatUtil.sendTextMsg(msg,content);
        }
    }

    /**
     * 查询所有的命令
     *
     * @param content
     */
    private void handleCommand(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag){
            List<CommandEntity> commandEntities = commandMapper.queryCommand(null, null,null);
            String msg = "";
            for (CommandEntity commandEntity : commandEntities) {
                msg += commandEntity.getCommand() + " -> " + commandEntity.getFunction() + "\n";
            }
            weChatUtil.sendTextMsg(msg, content);
        }
    }

    /**
     * 启用京东ck
     *
     * @param content
     */
    private void handleEnableCk(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag){
            String ck = content.getString("msg").replace("启用", "").replace(" ", "");
            //查询所有青龙
            List<QlEntity> qlEntities = qlMapper.queryQls(null);
            boolean isReturn = false;
            for (QlEntity qlEntity : qlEntities) {
                if (isReturn) {
                    return;
                }
                List<QlEnv> envs = qlUtil.getEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
                for (QlEnv env : envs) {
                    if ("JD_COOKIE".equals(env.getName()) && env.getValue().contains(ck)) {
                        ArrayList<Integer> ids = new ArrayList<>();
                        ids.add(env.getId());
                        qlUtil.enableEnv(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken(), ids);
                        isReturn = true;
                        weChatUtil.sendTextMsg("启用成功", content);
                        break;
                    }
                }
            }
            weChatUtil.sendTextMsg("启用失败，该ck不存在", content);
        }
    }

    /**
     * 禁用京东ck
     *
     * @param content
     */
    private void handleDisableCk(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag){
            String ck = content.getString("msg").replace("禁用", "").replace(" ", "");
            //查询所有青龙
            List<QlEntity> qlEntities = qlMapper.queryQls(null);
            boolean isReturn = false;
            for (QlEntity qlEntity : qlEntities) {
                if (isReturn) {
                    return;
                }
                List<QlEnv> envs = qlUtil.getEnvs(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken());
                for (QlEnv env : envs) {
                    if ("JD_COOKIE".equals(env.getName()) && env.getValue().contains(ck)) {
                        ArrayList<Integer> ids = new ArrayList<>();
                        ids.add(env.getId());
                        qlUtil.disableEnv(qlEntity.getUrl(), qlEntity.getTokenType(), qlEntity.getToken(), ids);
                        isReturn = true;
                        weChatUtil.sendTextMsg("禁用成功", content);
                        break;
                    }
                }
            }
            weChatUtil.sendTextMsg("禁用失败，该ck不存在", content);
        }
    }

    /**
     * 微信安全措施
     * @return
     */
    public boolean wxSafeCheck(String operate,JSONObject content){
        String iswxsafe = systemParamUtil.querySystemParam("ISWXSAFE");
        if ("否".equals(iswxsafe)){
            return true;
        }
        String from_wxid = content.getString("from_wxid");
        //限制一天只能查3次
        String timesStr = redis.get(from_wxid + operate);
        if (StringUtils.isEmpty(timesStr)) {
            redis.put(from_wxid + operate, DateUtil.formatDateTime(DateUtil.date()));
            return true;
        } else {
            //上次查询时间
            DateTime lastTime = DateUtil.parse(redis.get(from_wxid + operate));
            DateTime limitTime = DateUtil.offsetSecond(lastTime, 30);
            if (limitTime.getTime() > DateUtil.date().getTime()) {
//                weChatUtil.sendTextMsg("抱歉，请在" + DateUtil.formatDateTime(limitTime) + "后再试", content);
                return false;
            }
            redis.put(from_wxid + operate, DateUtil.formatDateTime(DateUtil.date()));
            return true;
        }
    }

    /**
     * 查询京东资产
     *
     * @param content
     */
    @Override
    public void handleQueryJdAssets(JSONObject content) {
        String from_wxid = content.getString("from_wxid");
        //限制一天只能查3次
        String timesStr = redis.get(from_wxid + "QueryTimes");
        if (StringUtils.isNotEmpty(timesStr)) {
            Integer times = Integer.parseInt(timesStr);
            if (times > 3) {
                weChatUtil.sendTextMsg("抱歉，今日查询次数已耗尽，请明日再查询（robot保护京东账号策略：一天只能查三次）", content);
                return;
            }
            //限制查询间隔3个小时
            //上次查询时间
            DateTime lastTime = DateUtil.parse(redis.get(from_wxid + "QueryTime"));
            DateTime limitTime = DateUtil.offsetHour(lastTime, 3);
            if (limitTime.getTime() > DateUtil.date().getTime()) {
                weChatUtil.sendTextMsg("抱歉，请在" + DateUtil.formatDateTime(limitTime) + "后查询（robot保护京东账号策略：查询间隔3小时）", content);
                return;
            }
        }
        User userQuery = new User();
        userQuery.setWxid(from_wxid);
        User user = userService.queryUser(userQuery);
        if (StringUtils.isEmpty(user.getJdPtPin().replace("#",""))||"null".equals(user.getJdPtPin().replace("#",""))) {
            weChatUtil.sendTextMsg("未查询到账号，请先登陆", content);
            return;
        }
        String jdPtPins = user.getJdPtPin();
        String[] split = jdPtPins.split("#");
        for (String jdPtPin : split) {
            if (StringUtils.isEmpty(jdPtPin)){
                continue;
            }
            JdCkEntity jdCkEntityQuery = new JdCkEntity();
            jdCkEntityQuery.setPtPin(jdPtPin);
            JdCkEntity jdCkEntity = jdCkMapper.queryCk(jdCkEntityQuery);
            if (jdCkEntity != null && jdCkEntity.getStatus() == 1) {
                weChatUtil.sendTextMsg(jdPtPin+"京东账号已过期，请重新登陆", content);
                continue;
            }
            String jdBeanChange = null;
            try {
                jdBeanChange = jdBeanChangeUtil.getJdBeanChange(jdCkEntity);
                weChatUtil.sendTextMsg(jdBeanChange, content);
            } catch (Exception e) {
                e.printStackTrace();
                weChatUtil.sendTextMsg(jdPtPin+"京东资产查询接口异常，请稍后再试", content);
                continue;
            }
        }
        if (StringUtils.isEmpty(timesStr)){
            redis.put(from_wxid + "QueryTimes", "1");
            redis.put(from_wxid + "QueryTime", DateUtil.formatDateTime(DateUtil.date()));
        }else {
            Integer times = Integer.parseInt(timesStr);
            redis.put(from_wxid + "QueryTimes", String.valueOf(times + 1));
            redis.put(from_wxid + "QueryTime", DateUtil.formatDateTime(DateUtil.date()));
        }
    }

    /**
     * 支付宝红包
     *
     * @param content
     */
    private void handleZfbHb(JSONObject content) {
        String elmurl = systemParamUtil.querySystemParam("ZFBHBUL");
        if (StringUtils.isEmpty(elmurl)) {
            weChatUtil.sendTextMsg("请先设置支付宝红包图片 设置 饿了么图片 你的饿了么图片地址", content);
        } else {
            weChatUtil.sendTextMsg("扫描下方二维码获取支付宝大额现金红包", content);
            weChatUtil.sendImageMsg(elmurl, content);
        }
    }

    /**
     * 推送支付宝红包
     *
     * @param content
     */
    private void handleTsZfbHb(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            if (StringUtils.isNotEmpty(from_group)) {
                Group group = groupMapper.queryGroupByGroupIdAndFunctionType(from_group, FunctionType.zfbhbts);
                if (group != null) {
                    weChatUtil.sendTextMsg("已开启支付宝红包推送", content);
                } else {
                    group = new Group();
                    group.setGroupid(from_group);
                    group.setGroupName(content.getString("from_group_name"));
                    group.setFunctionType(FunctionType.zfbhbts);
                    int i = groupMapper.addGroup(group);
                    if (i == 0) {
                        weChatUtil.sendTextMsg("开启推送失败", content);
                    } else {
                        weChatUtil.sendTextMsg("开启推送成功", content);
                    }
                }
            }
        }
    }

    /**
     * 取消推送支付宝红包
     *
     * @param content
     */
    private void handleQxTsZfbHb(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            int i = groupMapper.deleteGroupByGroupIdAndFunctionType(from_group, FunctionType.zfbhbts);
            if (i > 0) {
                weChatUtil.sendTextMsg("取消推送支付宝红包成功", content);
            } else {
                weChatUtil.sendTextMsg("此群并未设置推送支付宝红包，无法取消", content);
            }
        }
    }

    /**
     * 羊了个羊
     *
     * @param content
     */
    private void handleYLGYt(JSONObject content) {
        String from_wxid = content.getString("from_wxid");
        String msg = content.getString("msg");
        String token = msg.replace("羊t ", "");
        weChatUtil.sendTextMsg("请在10s内输入需要刷的次数：(输入q退出当前操作)", content);
        redis.put(from_wxid + "ylgyUid", "", 15 * 1000);
        redis.put(from_wxid + "ylgyToken", token, 15 * 1000);
        redis.put(from_wxid + "operate", "brushylgytimes", 11 * 1000);
    }

    /**
     * 羊了个羊
     *
     * @param content
     */
    private void handleYLGY(JSONObject content) {
        String from_wxid = content.getString("from_wxid");
        String msg = content.getString("msg");
        String uid = msg.replace("羊 ", "");
        if (StringUtils.isEmpty(uid)) {
            weChatUtil.sendTextMsg("请传入羊了个羊的uid", content);
            return;
        }
        String token = YlgyUtils.getYlgyToken(uid);
        if (StringUtils.isNotEmpty(token)) {
            weChatUtil.sendTextMsg("您的羊了个羊的token：", content);
            weChatUtil.sendTextMsg(token, content);
            redis.put(from_wxid + "ylgyUid", uid, 15 * 1000);
            redis.put(from_wxid + "ylgyToken", token, 15 * 1000);
            redis.put(from_wxid + "operate", "brushylgy", 11 * 1000);
            weChatUtil.sendTextMsg("是否需要代刷，需要请在10s内输入: y", content);
        } else {
            weChatUtil.sendTextMsg("获取羊了个羊token失败，请重试获取联系管理员", content);
        }
    }

    /**
     * 老色批
     *
     * @param content
     */
    private void handleLSP(JSONObject content) {
        if (wxSafeCheck("lsp",content)){
            String url = HttpRequest.get("https://api.uomg.com/api/rand.img3?format=images").execute().header("location");
            weChatUtil.sendImageMsg(url, content);
        }
    }

    /**
     * 处理当前操作
     *
     * @param content
     */
    private void handleOperate(JSONObject content, String operate, String msg, String from_wxid) {
        //京东登陆
        if ("readPhone".equals(operate)) {
            log.info("京东登陆-readPhone");
            if (VerifyUtil.verifyPhone(msg)) {
                redis.put(from_wxid + "phone", msg, 5 * 60 * 1000);
                if (sendSMS(msg)) {
                    weChatUtil.sendTextMsg("请在五分钟内输入验证码：(输入q退出当前操作)", content);
                    redis.put(from_wxid + "operate", "readIdentifyingCode", 5 * 60 * 1000);
                } else {
                    redis.remove(from_wxid + "operate");
                    weChatUtil.sendTextMsg("登陆异常，请联系管理员或稍后重试", content);
                }
            } else {
                weChatUtil.sendTextMsg("非法手机号，请在一分钟内重新输入手机号：(输入q退出当前操作)", content);
                redis.put(from_wxid + "operate", "readPhone", 60 * 1000);
            }
            return;
        }
        //京东登陆
        if ("readIdentifyingCode".equals(operate)) {
            log.info("京东登陆-readIdentifyingCode");
            String phone = redis.get(from_wxid + "phone");
            String ck = verifyCode(phone, msg);
            if (StringUtils.isEmpty(ck)) {
                redis.remove(from_wxid + "operate");
                weChatUtil.sendTextMsg("登陆异常，请联系管理员或稍后重试", content);
            } else {
                addJdCk(ck,content);
            }
            return;
        }

        //羊了个羊
        if ("brushylgy".equals(operate)) {
            if ("y".equals(msg)) {
                weChatUtil.sendTextMsg("请在10s内输入需要刷的次数：(输入q退出当前操作)", content);
                redis.put(from_wxid + "ylgyUid", redis.get(from_wxid + "ylgyUid"), 15 * 1000);
                redis.put(from_wxid + "ylgyToken", redis.get(from_wxid + "ylgyToken"), 15 * 1000);
                redis.put(from_wxid + "operate", "brushylgytimes", 11 * 1000);
            }
        }

        if ("brushylgytimes".equals(operate)) {
            if (NumberUtil.isInteger(msg)) {
                Integer times = Integer.parseInt(msg);
                if (times <= 0 || times > 1000000) {
                    weChatUtil.sendTextMsg("输入数据异常，请重新输入(输入q退出当前操作)", content);
                    redis.put(from_wxid + "ylgyUid", redis.get(from_wxid + "ylgyUid"), 15 * 1000);
                    redis.put(from_wxid + "ylgyToken", redis.get(from_wxid + "ylgyToken"), 15 * 1000);
                    redis.put(from_wxid + "operate", "brushylgytimes", 11 * 1000);
                    return;
                }
                String uid = redis.get(from_wxid + "ylgyUid");
                String token = redis.get(from_wxid + "ylgyToken");
                int i = ylgyMapper.addWire(uid, token, times);
                if (i == 1) {
                    Integer count = ylgyMapper.queryCount();
                    weChatUtil.sendTextMsg("添加成功，开刷。。。", content);
                }
            } else {
                weChatUtil.sendTextMsg("输入数据异常，请重新输入(输入q退出当前操作)", content);
                redis.put(from_wxid + "ylgyUid", redis.get(from_wxid + "ylgyUid"), 15 * 1000);
                redis.put(from_wxid + "ylgyToken", redis.get(from_wxid + "ylgyToken"), 15 * 1000);
                redis.put(from_wxid + "operate", "brushylgytimes", 11 * 1000);
            }
        }
    }

    private void addJdCk(String ck,JSONObject content){
        String ptPin = "";
        Matcher matcher = jdPinPattern.matcher(ck);
        if (matcher.find()) {
            ptPin = matcher.group(1);
        }
        String wxpusherUid = "";
        //生成wxpusher二维码
        if (wxpusherUtil.getWxpusherCode(content)) {
            try {
                Thread.sleep(11 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                for (int i = 0; i < 2; i++) {
                    weChatUtil.sendTextMsg("正在配置资产推送中，请稍后。。。", content);
                    wxpusherUid = wxpusherUtil.getWxpusherUid(content);
                    if (StringUtils.isNotEmpty(wxpusherUid)) {
                        break;
                    }
                    try {
                        Thread.sleep(11 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (StringUtils.isEmpty(wxpusherUid)) {
                weChatUtil.sendTextMsg("未扫码，资产推送配置失败", content);
            }
            //保存ck
            qlService.addJdCk(content, ck, ptPin, wxpusherUid);
        } else {
            //保存ck
            qlService.addJdCk(content, ck, ptPin, "");
        }
        //保存本地cookie
        JdCkEntity jdCkEntity = new JdCkEntity();
        jdCkEntity.setCk(ck);
        jdCkEntity.setPtPin(ptPin);
        jdCkEntity.setStatus(0);
        jdCkEntity.setLevel(2);
        ckService.addCkWithOutCheck(jdCkEntity);
        //保存user
        userService.saveUser(content, content.getString("from_wxid"), ptPin, wxpusherUid);
        redis.remove(content.getString("from_wxid") + "operate");
    }

    /**
     * 博客
     *
     * @param content
     */
    private void handleFunctionBoKe(JSONObject content) {
        String bokeUrl = systemParamUtil.querySystemParam("BOKEURL");
        if (StringUtils.isEmpty(bokeUrl)) {
            weChatUtil.sendTextMsg("尚未设置博客地址", content);
        } else {
            weChatUtil.sendTextMsg(bokeUrl, content);
        }
    }

    /**
     * 京东登陆
     *
     * @param content
     */
    private void handleJdLogin(JSONObject content) {
        String from_wxid = content.getString("from_wxid");
        String jdlonginurl = systemParamUtil.querySystemParam("JDLONGINURL");
        if (StringUtils.isEmpty(jdlonginurl)) {
            weChatUtil.sendTextMsg("尚未设置京东登陆地址，请联系管理员", content);
            return;
        }
        weChatUtil.sendTextMsg("请在一分钟内输入手机号：(输入q退出当前操作)", content);
        redis.put(from_wxid + "operate", "readPhone", 60 * 1000);

    }

    /**
     * 加群
     *
     * @param content
     */
    private void handleAddGroup(JSONObject content) {
        weChatUtil.InviteInGroup(content);
    }

    /**
     * 取消饿了么推送
     *
     * @param content
     */
    private void handleQxElmTs(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            int i = groupMapper.deleteGroupByGroupIdAndFunctionType(from_group, FunctionType.elmts);
            if (i > 0) {
                weChatUtil.sendTextMsg("取消饿了么推送成功", content);
            } else {
                weChatUtil.sendTextMsg("此群并未设置茅台洋河监控，无法取消", content);
            }
        }
    }

    /**
     * 饿了么推送
     *
     * @param content
     */
    private void handleElmTs(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            if (StringUtils.isNotEmpty(from_group)) {
                Group group = groupMapper.queryGroupByGroupIdAndFunctionType(from_group, FunctionType.elmts);
                if (group != null) {
                    weChatUtil.sendTextMsg("已开启饿了么推送", content);
                } else {
                    group = new Group();
                    group.setGroupid(from_group);
                    group.setGroupName(content.getString("from_group_name"));
                    group.setFunctionType(FunctionType.elmts);
                    int i = groupMapper.addGroup(group);
                    if (i == 0) {
                        weChatUtil.sendTextMsg("开启推送失败", content);
                    } else {
                        weChatUtil.sendTextMsg("开启推送成功", content);
                    }
                }
            }
        }
    }

    /**
     * 取消推送摸鱼
     *
     * @param content
     */
    private void handleQxTsMy(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            int i = groupMapper.deleteGroupByGroupIdAndFunctionType(from_group, FunctionType.myts);
            if (i > 0) {
                weChatUtil.sendTextMsg("取消推送摸鱼成功", content);
            } else {
                weChatUtil.sendTextMsg("此群并未设置推送摸鱼，无法取消", content);
            }
        }
    }

    /**
     * 推送摸鱼
     *
     * @param content
     */
    private void handleTsMy(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            if (StringUtils.isNotEmpty(from_group)) {
                Group group = groupMapper.queryGroupByGroupIdAndFunctionType(from_group, FunctionType.myts);
                if (group != null) {
                    weChatUtil.sendTextMsg("已开启摸鱼推送", content);
                } else {
                    group = new Group();
                    group.setGroupid(from_group);
                    group.setGroupName(content.getString("from_group_name"));
                    group.setFunctionType(FunctionType.myts);
                    int i = groupMapper.addGroup(group);
                    if (i == 0) {
                        weChatUtil.sendTextMsg("开启推送失败", content);
                    } else {
                        weChatUtil.sendTextMsg("开启推送成功", content);
                    }
                }
            }
        }
    }

    /**
     * 取消推送微博
     *
     * @param content
     */
    private void handleQxTsWb(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            int i = groupMapper.deleteGroupByGroupIdAndFunctionType(from_group, FunctionType.wbts);
            if (i > 0) {
                weChatUtil.sendTextMsg("取消推送微博成功", content);
            } else {
                weChatUtil.sendTextMsg("此群并未设置推送微博，无法取消", content);
            }
        }
    }

    /**
     * 推送微博
     *
     * @param content
     */
    private void handleTsWb(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            if (StringUtils.isNotEmpty(from_group)) {
                Group group = groupMapper.queryGroupByGroupIdAndFunctionType(from_group, FunctionType.wbts);
                if (group != null) {
                    weChatUtil.sendTextMsg("已开启微博推送", content);
                } else {
                    group = new Group();
                    group.setGroupid(from_group);
                    group.setGroupName(content.getString("from_group_name"));
                    group.setFunctionType(FunctionType.wbts);
                    int i = groupMapper.addGroup(group);
                    if (i == 0) {
                        weChatUtil.sendTextMsg("开启推送失败", content);
                    } else {
                        weChatUtil.sendTextMsg("开启推送成功", content);
                    }
                }
            }
        }
    }

    /**
     * 取消茅台洋河监控
     *
     * @param content
     */
    private void handleQxMtYhJk(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            int i = groupMapper.deleteGroupByGroupIdAndFunctionType(from_group, FunctionType.mtyhjk);
            if (i > 0) {
                weChatUtil.sendTextMsg("取消茅台洋河监控成功", content);
            } else {
                weChatUtil.sendTextMsg("此群并未设置茅台洋河监控，无法取消", content);
            }
        }
    }

    /**
     * 监控茅台洋河
     *
     * @param content
     */
    private void handleJkMtYh(JSONObject content) {
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            //获取微信群号
            String from_group = content.getString("from_group");
            if (StringUtils.isNotEmpty(from_group)) {
                Group group = groupMapper.queryGroupByGroupIdAndFunctionType(from_group, FunctionType.mtyhjk);
                if (group != null) {
                    weChatUtil.sendTextMsg("已开启茅台洋河监控", content);
                } else {
                    group = new Group();
                    group.setGroupid(from_group);
                    group.setGroupName(content.getString("from_group_name"));
                    group.setFunctionType(FunctionType.mtyhjk);
                    int i = groupMapper.addGroup(group);
                    if (i == 0) {
                        weChatUtil.sendTextMsg("监控失败", content);
                    } else {
                        weChatUtil.sendTextMsg("监控成功", content);
                    }
                }
            }
        }
    }

    /**
     * 油价
     *
     * @param content
     */
    private void handleYouJia(JSONObject content) {
        String city = content.getString("msg").replace("油价", "").trim();
        if (StringUtils.isEmpty(city)) {
            weChatUtil.sendTextMsg("未输入需要查询的城市", content);
        }
        String key = systemParamUtil.querySystemParam("TIANXINGKEY");
        if (StringUtils.isEmpty(key)) {
            weChatUtil.sendTextMsg("请先去 https://www.tianapi.com 网站注册申请key，对机器人发送：设置天行key 你的key", content);
            return;
        }
        String resStr = HttpRequest.get("http://api.tianapi.com/oilprice/index?key=" + key + "&prov=" + city).execute().body();
        if (StringUtils.isEmpty(resStr)) {
            weChatUtil.sendTextMsg("接口异常，请尽快维护", content);
        } else {
            JSONObject res = JSONObject.parseObject(resStr);
            String newslist = res.getString("newslist");
            if (StringUtils.isEmpty(newslist)) {
                weChatUtil.sendTextMsg("未查到该城市油价,仅能查询省份油价", content);
            }
            List<JSONObject> jsonObjects = JSON.parseArray(newslist, JSONObject.class);
            if (jsonObjects.size() == 0) {
                weChatUtil.sendTextMsg("未查到该城市油价,仅能查询省份油价", content);
            }
            JSONObject jsonObject = jsonObjects.get(0);
            String msg = "城市：" + jsonObject.getString("prov") + "\r\n";
            msg += "p0：" + jsonObject.getString("p0") + "\r\n";
            msg += "p89：" + jsonObject.getString("p89") + "\r\n";
            msg += "p92：" + jsonObject.getString("p92") + "\r\n";
            msg += "p95：" + jsonObject.getString("p95") + "\r\n";
            msg += "p98：" + jsonObject.getString("p98") + "\r\n";
            msg += "更新时间：" + jsonObject.getString("time");
            weChatUtil.sendTextMsg(msg, content);
        }
    }

    /**
     * 功能列表
     *
     * @param content
     */
    private void handleFunctionList(JSONObject content) {
        weChatUtil.sendTextMsg(redis.get("functionList"), content);
    }

    /**
     * 设置系统参数
     *
     * @param content
     */
    private void handleSetSysParam(JSONObject content) {
        String msg = content.getString("msg").replace("设置", "").replace("关闭", "").trim();
        String[] split = msg.split(" ");
        if (split.length >= 1) {
            if (StringUtils.isEmpty(systemParamUtil.querySystemParam("WXMASTERS")) && "微信管理员".equals(split[0])) {
                //第一次设置管理员
                handleSetParam("WXMASTERS", "微信管理员", split[1], content);
            } else {
                boolean flag = weChatUtil.isMaster(content);
                if (flag) {
                    switch (split[0]) {
                        case "微信管理员":
                            handleSetParam("WXMASTERS", "微信管理员", split[1], content);
                            break;
                        case "天行key":
                            systemParamUtil.updateSystemParam("TIANXINGKEY", "天行key", split[1]);
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        case "和风key":
                            systemParamUtil.updateSystemParam("HEFENGKEY", "和风key", split[1]);
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        case "喝水提醒":
                            systemParamUtil.updateSystemParam("ISSENDWATER", "喝水提醒", split[1]);
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        case "喝水提醒推送":
                            handleSetParam("SENDWATERLIST", "喝水提醒推送", split[1], content);
                            break;
                        case "微博推送":
                            handleSetParam("SENDWEIBOLIST", "微博推送", split[1], content);
                            break;
                        case "饿了么图片":
                            systemParamUtil.updateSystemParam("ELMURL", "饿了么图片", split[1]);
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        case "官方群":
                            systemParamUtil.updateSystemParam("OFFICIALGROUP", "官方群", content.getString("from_group"));
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        case "京东登陆":
                            systemParamUtil.updateSystemParam("JDLONGINURL", "京东登陆地址", split[1]);
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        case "wxpusher token":
                            systemParamUtil.updateSystemParam("WXPUSHERTOKEN", "wxpusher token", split[1]);
                            weChatUtil.sendTextMsg("设置成功", content);
                            break;
                        default:
                            weChatUtil.sendTextMsg("命令有误", content);
                    }
                }
            }
        } else {
            weChatUtil.sendTextMsg("参数有误", content);
        }
    }

    /**
     * 查询微信管理员
     */
    public void handleQueryWXMasters(JSONObject content) {
        String masters = systemParamUtil.querySystemParam("WXMASTERS");
        if (StringUtils.isEmpty(masters)) {
            weChatUtil.sendTextMsg("尚未设置管理员，请先按照以下命令设置,多个请用@隔开", content);
            weChatUtil.sendTextMsg("设置微信管理员 你的uid", content);
        }
        boolean flag = weChatUtil.isMaster(content);
        if (flag) {
            weChatUtil.sendTextMsg(masters, content);
        }
    }

    /**
     * 查询群id
     *
     * @param content
     */
    public void handleGroupCode(JSONObject content) {
        String groupName = content.getString("msg").replace("群号","").trim();
        String msg = "";
        if (StringUtils.isEmpty(groupName)){
            msg = content.getString("from_group");
        }else {
            List<Group> groups = groupMapper.queryDistinctGroup(groupName,null);
            if (groups.size()==0){
                msg = "未查到"+groupName+"群";
            }else {
                for (Group group : groups) {
                    msg += group.getGroupName() + "\n  " + group.getGroupid() + "\n";
                }
            }
        }
        //发送人
        weChatUtil.sendTextMsg(msg, content);
    }


    /**
     * 查询我的uid
     *
     * @param content
     */
    public void handleMyUid(JSONObject content) {
        //发送人
        String from_wxid = content.getString("from_wxid");
        weChatUtil.sendTextMsg(from_wxid, content);
    }

    /**
     * 设置参数有多值
     *
     * @param content
     */
    public void handleSetParam(String code, String name, String value, JSONObject content) {
        //发送人
        String oldValue = systemParamUtil.querySystemParam(code);
        if (!oldValue.contains(value)) {
            //添加新的管理员
            if (StringUtils.isEmpty(oldValue)) {
                oldValue = value;
            } else {
                oldValue = oldValue + "#" + value;
            }
            boolean flag = systemParamUtil.updateSystemParam(code, name, oldValue);
            if (flag) {
                weChatUtil.sendTextMsg("设置成功", content);
            } else {
                weChatUtil.sendTextMsg("设置失败，系统异常", content);
            }
        } else {
            //已设置
            weChatUtil.sendTextMsg("设置成功", content);
        }
    }

    public void handleLast(JSONObject content, Integer num, String publicKey) {
        if ("微博".equals(publicKey)) {
            handleLastWeiBo(content, num);
        }

    }

    /**
     * 比价
     *
     * @param url
     */
    public void handleGoods(String url, JSONObject content) {
        String resStr = doGetGoods(url);
        if (StringUtils.isNotEmpty(resStr)) {
            JSONObject res = JSONObject.parseObject(resStr);
            String singleStr = res.getString("single");
            if (StringUtils.isNotEmpty(singleStr)) {
                JSONObject single = JSONObject.parseObject(singleStr);
                dohandleGoods(single, content);
            } else {
                //再尝试一次
                resStr = doGetGoods(url);
                if (StringUtils.isNotEmpty(resStr)) {
                    res = JSONObject.parseObject(resStr);
                    singleStr = res.getString("single");
                    if (StringUtils.isNotEmpty(singleStr)) {
                        JSONObject single = JSONObject.parseObject(singleStr);
                        dohandleGoods(single, content);
                    } else {
                        log.info("比价失败！！！");
                    }
                }
            }
        } else {
            //再尝试一次
            resStr = doGetGoods(url);
            if (StringUtils.isNotEmpty(resStr)) {
                JSONObject res = JSONObject.parseObject(resStr);
                String singleStr = res.getString("single");
                if (StringUtils.isNotEmpty(singleStr)) {
                    JSONObject single = JSONObject.parseObject(singleStr);
                    dohandleGoods(single, content);
                } else {
                    log.info("比价失败！！！");
                }
            }
        }
    }

    public void dohandleGoods(JSONObject single, JSONObject content) {
        String result = "";
        //商品名称
        String title = single.getString("title");
        result += "商品名称：" + title + "\r\n";
        //商品链接
        String goodsUrl = single.getString("url");
//            result += "商品链接：" + goodsUrl + "\r\n";
        //当前价格
        //当前价格
        String spmoney = single.getString("spmoney");
        result += "当前价格：" + spmoney + "\r\n";
        //当前价格状态
        String currentPriceStatus = single.getString("currentPriceStatus");
        result += "当前状态：" + currentPriceStatus + "\r\n";
        //趋势变动
        String changPriceRemark = single.getString("changPriceRemark");
        result += "趋势变动：" + changPriceRemark + "\r\n";
        //价格趋势
        String qushi = single.getString("qushi");
        result += "价格趋势：" + qushi + "\r\n";
        //大图
        String bigpic = single.getString("bigpic");

        //小图
        String smallpic = single.getString("smallpic");

        //最低价格
        String lowerPrice = single.getString("lowerPrice");
        result += "最低价格：" + lowerPrice + "\r\n";
        //最低日期  lowerDate -> /Date(1599753600000+0800)/
        String lowerDate = single.getString("lowerDate");
        Matcher matcher = lowerDatePattern.matcher(lowerDate);
        if (matcher.find()) {
            Date date = new Date(Long.valueOf(matcher.group(1)));
            lowerDate = DateUtil.format(date, "yyyy.MM.dd");
            result += "最低日期：" + lowerDate + "\r\n";
        }

        //价格趋势
        String jiagequshi = single.getString("jiagequshi");
        String[] split = jiagequshi.replace("[Date.UTC(", "").replace(")", "").replace("]", "").split(",");
        List<String> list = Arrays.asList(split);
        List<List<String>> lists = ListUtil.split(list, 5);
        EvictingQueue<String> priceChangeList = EvictingQueue.create(20);
        String price = "";
        for (List<String> object : lists) {
            String p = object.get(3);
            if (StringUtils.isEmpty(price)) {
                price = p;
                String raw = object.get(0) + "-" + object.get(1) + "-" + object.get(2) + "：" + price;
                priceChangeList.add(raw);
            } else {
                BigDecimal last = new BigDecimal(price);
                BigDecimal now = new BigDecimal(p);
                if (now.compareTo(last) != 0) {
                    price = p;
                    String raw = object.get(0) + "-" + object.get(1) + "-" + object.get(2) + "：" + price;
                    priceChangeList.add(raw);
                }
            }
        }
        if (priceChangeList.size() > 0) {
            result += "最近20次价格变动如下：\r\n";
            for (String s : priceChangeList) {
                result += s + "\r\n";
            }
        }
        weChatUtil.sendImageMsg(bigpic, content);
        weChatUtil.sendTextMsg(result, content);
    }

    /**
     * 调查询商品接口
     *
     * @param url
     * @return
     */
    public String doGetGoods(String url) {
        url = url.replaceAll("/", "%252F").replaceAll("\\?", "%253F").replaceAll("=", "%253D").replaceAll(":", "%253A").replaceAll("&", "%26");
        String body = "c_devid=2C5039AF-99D0-4800-BC36-DEB3654D202C&username=&qs=true&c_engver=1.2.35&c_devtoken=&c_devmodel=iPhone%20SE&c_contype=wifi&t=1537348981671&c_win=w_320_h_568&p_url=" + url + "&c_ostype=ios&jsoncallback=%3F&c_ctrl=w_search_trend0_f_content&methodName=getBiJiaInfo_wxsmall&c_devtype=phone&jgzspic=no&c_operator=%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8&c_appver=2.9.0&bj=false&c_dp=2&c_osver=10.3.3";
        String resStr = HttpRequest.post("https://apapia.manmanbuy.com/ChromeWidgetServices/WidgetServices.ashx")
                .header("Host", "apapia.manmanbuy.com")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .header("Proxy-Connection", "close")
                .header("Cookie", "ASP.NET_SessionId=uwhkmhd023ce0yx22jag2e0o; jjkcpnew111=cp46144734_1171363291_2017/11/25")
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X) AppleWebKit/603.3.8 (KHTML like Gecko) Mobile/14G60 mmbWebBrowse")
                .header("Content-Length", "457")
                .header("Accept-Encoding", "gzip")
                .header("Connection", "close")
                .body(body)
                .execute().body();
        return resStr;
    }

    /**
     * 摸鱼定时
     */
    @Override
    public void timeHandleMY() {
        List<Group> groups = groupMapper.queryGroupByFunctionType(FunctionType.myts);
        for (Group group : groups) {
            JSONObject content = new JSONObject();
            content.put("from_group", group.getGroupid());
            content.put("robot_wxid", systemParamUtil.querySystemParam("ROBORTWXID"));
            handleMoYu(content);
        }
    }

    /**
     * 微博定时
     */
    @Override
    public void timeHandleWB() {
        List<Group> groups = groupMapper.queryGroupByFunctionType(FunctionType.wbts);
        for (Group group : groups) {
            JSONObject content = new JSONObject();
            content.put("from_group", group.getGroupid());
            content.put("robot_wxid", systemParamUtil.querySystemParam("ROBORTWXID"));
            handleWeiBo(content);
        }
    }

    /**
     * 饿了么定时
     */
    @Override
    public void timeHandleELM() {
        String elmurl = systemParamUtil.querySystemParam("ELMURL");
        if (StringUtils.isNotEmpty(elmurl)) {
            List<Group> groups = groupMapper.queryGroupByFunctionType(FunctionType.elmts);
            for (Group group : groups) {
                JSONObject content = new JSONObject();
                content.put("from_group", group.getGroupid());
                content.put("robot_wxid", systemParamUtil.querySystemParam("ROBORTWXID"));
                weChatUtil.sendTextMsg("到饭点啦，饿了么扫码领大额红包！！！", content);
                weChatUtil.sendImageMsg(elmurl, content);
            }

        }
    }

    /**
     * 饿了么
     *
     * @param content
     */
    private void handleELM(JSONObject content) {
        String elmurl = systemParamUtil.querySystemParam("ELMURL");
        if (StringUtils.isEmpty(elmurl)) {
            weChatUtil.sendTextMsg("请先设置饿了么推广图片 设置 饿了么图片 你的饿了么图片地址", content);
        } else {
            weChatUtil.sendTextMsg("扫描下方二维码获取饿了么大额红包红包", content);
            weChatUtil.sendImageMsg(elmurl, content);
        }
    }

    /**
     * 买家秀
     *
     * @param content
     */
    private void handleMJX(JSONObject content) {
        if (wxSafeCheck("mjx",content)){
            weChatUtil.sendImageMsg("http://api.uomg.com/api/rand.img3", content);
        }
    }

    /**
     * 举牌
     *
     * @param content
     */
    private void handleJuPai(JSONObject content) {
        if (wxSafeCheck("jp", content)){
            String msg = content.getString("msg");
            msg = msg.replace("举牌", "").replace(" ", "");
            if (StringUtils.isEmpty(msg)) {
                weChatUtil.sendTextMsg("正确命令是：举牌 要举牌的内容", content);
            } else {
                weChatUtil.sendImageMsg("http://lkaa.top/API/pai/?msg=" + URLEncoder.encode(msg), content);
            }
        }
    }

    /**
     * 微博
     *
     * @param content
     */
    @Override
    public void handleWeiBo(JSONObject content) {
        redis.put("publicKey", "微博", DateUnit.SECOND.getMillis() * 60 * 2);
        String result = HttpRequest.get("https://weibo.com/ajax/statuses/hot_band").execute().body();
        if (StringUtils.isEmpty(result)) {
            weChatUtil.sendTextMsg("微博接口废拉，赶紧联系管理员维护！！！", content);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String bandListStr = jsonObject.getJSONObject("data").getString("band_list");
        List<JSONObject> bandLists = JSONArray.parseArray(bandListStr, JSONObject.class);
        //加入缓存
        redis.put("WbBandLists", bandListStr);
        String msg = "微博实时热搜：\r\n\r\n";
        int index = 1;
        for (JSONObject band : bandLists) {
            String realpos = band.getString("realpos");
            if (StringUtils.isNotEmpty(realpos)) {
                msg = msg + index + "." + band.getString("word") + "\r\n";
                index++;
            }
            if (index == 21) {
                break;
            }
        }
        weChatUtil.sendTextMsg(msg, content);
    }

    public void handleLastWeiBo(JSONObject content, Integer num) {
        String bandListStr = redis.get("WbBandLists");
        List<JSONObject> bandLists = JSONArray.parseArray(bandListStr, JSONObject.class);
        int index = 1;
        for (JSONObject band : bandLists) {
            String realpos = band.getString("realpos");
            if (StringUtils.isNotEmpty(realpos)) {
                if (index == num) {
                    String word = band.getString("word");
                    weChatUtil.sendTextMsg(word + "\r\n" + "https://s.weibo.com/weibo?q=%23" + URLEncoder.encode(word) + "%23", content);
                    break;
                }
                index++;
            }
        }
    }

    /*public static void main(String[] args) {
        Festival festival = new Festival("国庆节", 10, 1, false, 0L);
        Date now = new Date();
        int year = DateUtil.year(now);
        int month = 0;
        int day = 0;
        // 如果节日为农历，则取出对应的公历日
        if (festival.isChineseDate()) {
            DateTime festivalDay = new DateTime(new ChineseDate(year, festival.getMonth(), festival.getDay()).getGregorianDate().getTime());
            month = festivalDay.month()+1;
            day = festivalDay.dayOfMonth();
        } else {
            month = festival.getMonth();
            day = festival.getDay();
        }
        // 如果当前日期大于等于节日的月份和天数，则年数取下一年
        if (DateUtil.month(now)+1 > month) {
            year++;
        }else {
            if (DateUtil.month(now)+1 == month && DateUtil.dayOfMonth(now) >= day){
                year++;
            }
        }
        DateTime festivalDay;
        // 如果节日为农历，则取出对应的公历日
        if (festival.isChineseDate()) {
            festivalDay = new DateTime(new ChineseDate(year, festival.getMonth(), festival.getDay()).getGregorianDate().getTime());
        } else {
            festivalDay = DateUtil.parse(year + "-" + festival.getMonth() + "-" + festival.getDay());
        }
        // 计算当前日期和节日（公历日）的天数差 并保存
        System.out.println(DateUtil.betweenDay(now, festivalDay, false));
    }*/


    /**
     * 摸鱼
     *
     * @param content
     * @return
     */
    @Override
    public void handleMoYu(JSONObject content) {
        // 获取当前日期
        Date now = new Date();
        int year = DateUtil.year(now);
        // 添加节日
        List<Festival> festivalList = new ArrayList<>();
        // 清明节[计算后的日期为公历日]
        // 注意清明的需要进行计算的，21世纪的公式为：[Y*D+C]-L
        // 公式解读：Y=年数后2位，D=0.2422，L=闰年数，21世纪C=4.81，20世纪=5.59.
        int param = year - 2000;
        int qingMingDay = (int) (param * 0.2422 + 4.81) - param / 4;
        festivalList.add(new Festival("清明节", 4, qingMingDay, false, 0L));
        festivalList.add(new Festival("元旦", 1, 1, false, 0L));
        festivalList.add(new Festival("春节", 1, 1, true, 0L));
        festivalList.add(new Festival("劳动节", 5, 1, false, 0L));
        festivalList.add(new Festival("端午节", 5, 5, true, 0L));
        festivalList.add(new Festival("中秋节", 8, 15, true, 0L));
        festivalList.add(new Festival("国庆节", 10, 1, false, 0L));
        festivalList.add(new Festival("七夕节", 7, 7, true, 0L));
        festivalList.add(new Festival("情人节", 2, 14, false, 0L));
//        festivalList.add(new Festival("重阳节", 9, 9, true, 0L));
        // 获取节日时间差
        festivalList.forEach(this::getGregorianDayDiff);
        // 存放周末 【不剔除法定调休】【周末无需关心月/日】
        Festival weekend = new Festival("周末", 0, 0, false, DateUtil.betweenDay(new Date(), DateUtil.endOfWeek(new Date(), false), false));
        // 判断今天是不是周六、周日
        weekend.setToday(DateUtil.betweenDay(new Date(), DateUtil.endOfWeek(new Date(), false), false) == 0
                || DateUtil.betweenDay(new Date(), DateUtil.endOfWeek(new Date(), true), false) == 0);
        festivalList.add(weekend);
        // 根据时间差排序【正序】
        festivalList.sort((Comparator.comparing(Festival::getDiff)));
        // 打印文档
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【摸鱼办】提醒您：").append(DateUtil.format(now, "MM月dd日,")).append(this.timeOfDay(now)).append("好,摸鱼人!\r\n");
        stringBuffer.append("工作再累，一定不要忘记摸鱼哦！有事没事起身去茶水间， 去厕所， 去廊道走走别老在工位上坐着， 钱是老板的, 但命是自己的!\r\n");
        festivalList.forEach(festival -> {
            if (!festival.getToday()) {
                stringBuffer.append("距离").append(festival.getName()).append("还有不到：").append(festival.getDiff()).append("天\r\n");
            } else {
                stringBuffer.append("今天是").append(festival.getName()).append("，好好享受吧！\r\n");
            }
        });
        stringBuffer.append("为了放假加油吧！\n" +
                "上班是帮老板赚钱，摸鱼是赚老板的钱！\n" +
                "最后，祝愿天下所有摸鱼人，都能愉快的渡过每一天！\n");
        // 获取黄历信息
        Lunar date = new Lunar(year, DateUtil.month(now), DateUtil.dayOfMonth(now));
        stringBuffer.append("======================\r\n");
        stringBuffer.append("【今日黄历】 ").append(date.toFullString());
        weChatUtil.sendTextMsg(stringBuffer.toString(), content);
    }

    /**
     * 获取当前日期和节假日的公历日差
     *
     * @param festival 节日
     */
    private void getGregorianDayDiff(Festival festival) {
        // 获取当前日期
        Date now = new Date();
        int year = DateUtil.year(now);
        int month = 0;
        int day = 0;
        // 如果节日为农历，则取出今年对应的公历日
        if (festival.isChineseDate()) {
            DateTime festivalDay = new DateTime(new ChineseDate(year, festival.getMonth(), festival.getDay()).getGregorianDate().getTime());
            month = festivalDay.month()+1;
            day = festivalDay.dayOfMonth();
        } else {
            month = festival.getMonth();
            day = festival.getDay();
        }
        // 如果当前日期大于等于节日的月份和天数，则年数取下一年
        if (DateUtil.month(now)+1 > month) {
            year++;
        }else {
            if (DateUtil.month(now)+1 == month && DateUtil.dayOfMonth(now) >= day){
                year++;
            }
        }
        DateTime festivalDay;
        // 如果节日为农历，则取出对应的公历日
        if (festival.isChineseDate()) {
            festivalDay = new DateTime(new ChineseDate(year, festival.getMonth(), festival.getDay()).getGregorianDate().getTime());
        } else {
            festivalDay = DateUtil.parse(year + "-" + festival.getMonth() + "-" + festival.getDay());
        }
        // 计算当前日期和节日（公历日）的天数差 并保存
        festival.setDiff(DateUtil.betweenDay(now, festivalDay, false));
        festival.setToday(DateUtil.isSameDay(now, festivalDay));
    }

    /**
     * 获取当前时间的时间段
     *
     * @param date 时间
     * @return
     */
    private String timeOfDay(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String str = df.format(date);
        int a = Integer.parseInt(str);
        if (a >= 0 && a <= 6) {
            return "凌晨";
        }
        if (a > 6 && a <= 11) {
            return "上午";
        }
        if (a > 11 && a <= 13) {
            return "中午";
        }
        if (a > 13 && a <= 18) {
            return "下午";
        }
        if (a > 18 && a <= 24) {
            return "晚上";
        }
        return null;
    }


    /**
     * 天气
     * 传递对应的城市名 南京
     * 如存在重复名称。则传递上级行政单位名称
     * -- 规则：上级城市名称 查询城市名称
     * -- 说明：以空格分隔 例：【南京 鼓楼】【徐州 鼓楼】
     *
     * @param content content
     */
    public void handleWeather(JSONObject content) {
        StringBuilder result = new StringBuilder();
        String key = systemParamUtil.querySystemParam("HEFENGKEY");
        if (StringUtils.isEmpty(key)) {
            weChatUtil.sendTextMsg("请先去 https://dev.qweather.com 网站注册申请key并配置系统参数", content);
            return;
        }
        try {
            String msg = content.getString("msg");
            String cityName = msg.replace("天气", "");
            String[] split = cityName.split(" ");
            JSONObject city = heFengWeatherUtil.getCity(split[split.length - 1], split.length > 1 ? split[0] : "", key);
            JSONObject now = heFengWeatherUtil.now(city.getInteger("id"), key);
            JSONObject warningNow = heFengWeatherUtil.warningNow(city.getInteger("id"), key);
            String name = "";
            if (!city.getString("name").equals(city.getString("adm2"))) {
                name = city.getString("adm2") + " " + city.getString("name");
            } else {
                name = city.getString("name");
            }
            result.append(name).append("\r\n")
                    .append("温度：").append(now.getString("temp")).append("℃\r\n")
                    .append("体感：").append(now.getString("feelsLike")).append("℃\r\n")
                    .append("湿度：").append(now.getString("humidity")).append("%\r\n")
                    .append("能见度：").append(now.getString("vis")).append("KM\r\n")
                    .append("天气：").append(now.getString("text")).append("\r\n")
                    .append("风向：").append(now.getString("windDir")).append("\r\n")
                    .append("风力：").append(now.getString("windScale")).append("级\r\n")
                    .append("降雨量：").append(now.getString("precip")).append("毫升\r\n");
            if (Objects.nonNull(warningNow)) {
                result.append("预警信息：").append(warningNow.getString("text")).append("\r\n");
            }

        } catch (Exception e) {
            result.append(e.getMessage());
        }
        weChatUtil.sendTextMsg(result.toString(), content);
    }

    /**
     * 喝水
     */
    @Override
    public void handleWater() {
        String issendwater = systemParamUtil.querySystemParam("ISSENDWATER");
        if ("1".equals(issendwater)) {
            String robortwxid = systemParamUtil.querySystemParam("ROBORTWXID");
            String SENDWATERLIST = systemParamUtil.querySystemParam("SENDWATERLIST");
            String msg = "亲爱的宝，记得多喝水喔，爱你喔";
            String[] list = SENDWATERLIST.split("#");
            for (int i = 0; i < list.length; i++) {
                String from_wxid = list[i];
                JSONObject content = new JSONObject();
                content.put("robot_wxid", robortwxid);
                content.put("from_wxid", from_wxid);
                weChatUtil.sendTextMsg(msg, content);
            }
        }
    }

    /**
     * 发送刷步数请求
     *
     * @param username   用户名手机号
     * @param password   密码
     * @param step       步数
     * @param minstep
     * @param maxstep
     * @param expiryTime
     * @return
     */
    @Override
    public Result updateStep(String username, String password, Integer step, boolean istime, Integer minstep, Integer maxstep, String expiryTime) {
        Result res = XiaoMiUtil.sport(username, password, String.valueOf(step));
        if ("200".equals(res.getCode()) && istime) {
            //添加数据库
            wxbsMapper.addZh(username, password, minstep, maxstep, expiryTime);
        }
        return res;
    }

    @Async("asyncServiceExecutor")
    @Override
    public void timeWxbs() {
        List<Wxbs> zhs = wxbsMapper.queryZhs();
        for (Wxbs zh : zhs) {
            try {
                //判断是否过期
                String expiryTime = zh.getExpiryTime();
                if (StringUtils.isNotEmpty(expiryTime) && DateUtil.parse(expiryTime).getTime() < DateUtil.date().getTime()) {
                    wxbsMapper.delete(zh.getId());
                    continue;
                }
                Integer minstep = zh.getMinstep();
                Integer maxstep = zh.getMaxstep();
                XiaoMiUtil.sport(zh.getUserName(), zh.getPassWord(), String.valueOf(RandomUtil.randomInt(minstep, maxstep)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void timeHandleZfbHb() {
        List<Group> groups = groupMapper.queryGroupByFunctionType(FunctionType.zfbhbts);
        for (Group group : groups) {
            JSONObject content = new JSONObject();
            content.put("from_group", group.getGroupid());
            content.put("robot_wxid", systemParamUtil.querySystemParam("ROBORTWXID"));
            handleZfbHb(content);
        }
    }


    /**
     * 早上好
     */
    public void goodMorning() {
        //1，配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId("");
        wxStorage.setSecret(" ");
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        //2,推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser("用户 微信id")
                .templateId("消息模板id")
                .build();
        //3,如果是正式版发送模版消息，这里需要配置你的信息
        Weather weather = WeatherUtils.getWeather();
        Map<String, String> map = CaiHongPiUtils.getEnsentence();
        templateMessage.addData(new WxMpTemplateData("riqi", weather.getDate() + "  " + weather.getWeek(), "#00BFFF"));
        templateMessage.addData(new WxMpTemplateData("tianqi", weather.getText_now(), "#00FFFF"));
        templateMessage.addData(new WxMpTemplateData("low", weather.getLow() + "", "#173177"));
        templateMessage.addData(new WxMpTemplateData("temp", weather.getTemp() + "", "#EE212D"));
        templateMessage.addData(new WxMpTemplateData("high", weather.getHigh() + "", "#FF6347"));
        templateMessage.addData(new WxMpTemplateData("windclass", weather.getWind_class() + "", "#42B857"));
        templateMessage.addData(new WxMpTemplateData("winddir", weather.getWind_dir() + "", "#B95EA3"));
        templateMessage.addData(new WxMpTemplateData("caihongpi", CaiHongPiUtils.getCaiHongPi(), "#FF69B4"));
        templateMessage.addData(new WxMpTemplateData("lianai", JiNianRiUtils.getLianAi() + "", "#FF1493"));
        templateMessage.addData(new WxMpTemplateData("shengri1", JiNianRiUtils.getBirthdayJo() + "", "#FFA500"));
        templateMessage.addData(new WxMpTemplateData("shengri2", JiNianRiUtils.getBirthdayHui() + "", "#FFA500"));
        templateMessage.addData(new WxMpTemplateData("en", map.get("en") + "", "#C71585"));
        templateMessage.addData(new WxMpTemplateData("zh", map.get("zh") + "", "#C71585"));
        String beizhu = "❤";
        if (JiNianRiUtils.getLianAi() % 365 == 0) {
            beizhu = "今天是恋爱" + (JiNianRiUtils.getLianAi() / 365) + "周年纪念日！";
        }
        if (JiNianRiUtils.getBirthdayJo() == 0) {
            beizhu = "今天是生日，生日快乐呀！";
        }
        if (JiNianRiUtils.getBirthdayHui() == 0) {
            beizhu = "今天是生日，生日快乐呀！";
        }
        templateMessage.addData(new WxMpTemplateData("beizhu", beizhu, "#FF0000"));

        try {
            System.out.println(templateMessage.toJson());
            System.out.println(wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage));
        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * nark发送手机号
     *
     * @param phone
     * @return
     */
    public boolean sendSMS(String phone) {
        String jdlonginurl = systemParamUtil.querySystemParam("JDLONGINURL").replace("login", "");
        if (!jdlonginurl.endsWith("/")) {
            jdlonginurl += "/";
        }
        JSONObject body = new JSONObject();
        body.put("Phone", phone);
        body.put("qlkey", 0);
        String resultStr = HttpRequest.post(jdlonginurl + "api/SendSMS")
                .body(body.toJSONString())
                .execute().body();
        log.info("nark发送手机号结果：{}",resultStr);
        if (StringUtils.isNotEmpty(resultStr)) {
            JSONObject res = JSONObject.parseObject(resultStr);
            if (res.getBoolean("success")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * nark发送验证码
     *
     * @param phone
     * @return
     */
    public String verifyCode(String phone, String code) {
        String jdlonginurl = systemParamUtil.querySystemParam("JDLONGINURL").replace("login", "");
        if (!jdlonginurl.endsWith("/")) {
            jdlonginurl += "/";
        }
        JSONObject body = new JSONObject();
        body.put("Phone", phone);
        body.put("QQ", "");
        body.put("qlkey", 0);
        body.put("Code", code);
        String resultStr = HttpRequest.post(jdlonginurl + "api/VerifyCode")
                .body(body.toJSONString())
                .execute().body();
        if (StringUtils.isNotEmpty(resultStr)) {
            log.info("手机号{}短信登陆结果：{}", phone, resultStr);
            JSONObject res = JSONObject.parseObject(resultStr);
            if (res.getBoolean("success")) {
                return res.getJSONObject("data").get("ck").toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}
