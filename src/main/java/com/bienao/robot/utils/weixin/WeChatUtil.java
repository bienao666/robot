package com.bienao.robot.utils.weixin;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class WeChatUtil {

    @Value("${vlw.url}")
    private String vlwUrl;

    @Value("${vlw.token}")
    private String vlmToken;

    @Autowired
    private SystemParamUtil systemParamUtil;


    /**
     * 发送文字消息
     */
    public void sendTextMsg(String msg, JSONObject content){
        JSONObject body = new JSONObject();
        String from_group = content.getString("from_group");
        if (StringUtils.isNotEmpty(from_group)){
            //群聊消息
            //对象WXID（好友ID/群ID/公众号ID）
            body.put("to_wxid",from_group);
        }else {
            //私聊消息
            //对象WXID（好友ID/群ID/公众号ID）
            body.put("to_wxid",content.getString("from_wxid"));
        }
        //消息
        body.put("msg",msg);
        //密钥
        body.put("token",vlmToken);
        //机器人ID
        body.put("robot_wxid",content.getString("robot_wxid"));
        //API名
        body.put("api","SendTextMsg");
        String result = HttpRequest.post(vlwUrl)
                .header("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)")
                .header("Content-Type", "application/json")
                .body(body.toJSONString())
                .execute().body();
        if (StringUtils.isEmpty(result)){
            log.info("发送文字消息接口调用失败");
            log.info("{}信息发送失败",body.toJSONString());
        }else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if ("200".equals(jsonObject.getString("Code"))){
                log.info("发送文字消息接口调用失败");
                log.info("{}信息发送失败",body.toJSONString());
            }else {
                log.info("发送文字消息成功");
            }
        }
    }

    public void sendTextMsgToMaster(String msg){
        JSONObject content = new JSONObject();
        String robortwxid = systemParamUtil.querySystemParam("ROBORTWXID");
        String wxmasters = systemParamUtil.querySystemParam("WXMASTERS");
        String[] split = wxmasters.split("#");
        for (String master : split) {
            if (StringUtils.isNotEmpty(master)){
                content.put("robot_wxid",robortwxid);
                content.put("from_wxid",master);
                sendTextMsg(msg,content);
            }
        }
    }

    /**
     * 发送图片消息
     */
    public void sendImageMsg(String urlPath, JSONObject content){
        JSONObject body = new JSONObject();
        String from_group = content.getString("from_group");
        if (StringUtils.isNotEmpty(from_group)){
            //群聊消息
            //对象WXID（好友ID/群ID/公众号ID）
            body.put("to_wxid",from_group);
        }else {
            //私聊消息
            //对象WXID（好友ID/群ID/公众号ID）
            body.put("to_wxid",content.getString("from_wxid"));
        }
        //本地图片文件的绝对路径 或 网络图片url 或 图片base64编码
        body.put("path",urlPath);
        //密钥
        body.put("token",vlmToken);
        //机器人ID
        body.put("robot_wxid",content.getString("robot_wxid"));
        //API名
        body.put("api","SendImageMsg");
        String result = HttpRequest.post(vlwUrl)
                .header("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)")
                .header("Content-Type", "application/json")
                .body(body.toJSONString())
                .execute().body();
        if (StringUtils.isEmpty(result)){
            log.info("发送图片消息接口调用失败");
            log.info("{}信息发送失败",body.toJSONString());
        }else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if ("200".equals(jsonObject.getString("Code"))){
                log.info("发送图片消息接口调用失败");
                log.info("{}信息发送失败",body.toJSONString());
            }else {
                log.info("发送图片消息成功");
            }
        }
    }

    /**
     * 同意好友请求
     */
    public void agreeFriendVerify(JSONObject content){
        JSONObject body = new JSONObject();
        //密钥
        body.put("token",vlmToken);
        //机器人ID
        body.put("robot_wxid",content.getString("robot_wxid"));
        //API名
        body.put("api","AgreeFriendVerify");
        //v1
        body.put("v1",content.getString("v1"));
        //v2
        body.put("v2",content.getString("v2"));
        //type
        body.put("type",content.getInteger("type"));
        String result = HttpRequest.post(vlwUrl)
                .header("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)")
                .header("Content-Type", "application/json")
                .body(body.toJSONString())
                .execute().body();
        if (StringUtils.isEmpty(result)){
            log.info("同意好友请求接口调用失败");
            log.info("{}同意好友请求失败",body.toJSONString());
        }else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if ("200".equals(jsonObject.getString("Code"))){
                log.info("同意好友请求接口调用失败");
                log.info("{}同意好友请求失败",body.toJSONString());
            }else {
                log.info("同意好友请求成功");
            }
        }
    }

    /**
     * 邀请好友入群_直接拉
     * @return
     */
    public void InviteInGroup(JSONObject content){
        String officialgroup = systemParamUtil.querySystemParam("OFFICIALGROUP");
        if (StringUtils.isEmpty(officialgroup)){
            //未设置个人官方群、通知管理员
            String masters = systemParamUtil.querySystemParam("WXMASTERS");
            if (StringUtils.isNotEmpty(masters)){
                String[] split = masters.split("@");
                for (String master : split) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("robot_wxid",content.getString("robot_wxid"));
                    jsonObject.put("from_wxid",master);
                    sendTextMsg("新的伙伴想要加群，请先去群对我发送：设置 官方群",jsonObject);
                }
            }
            sendTextMsg("加群失败，请联系群主",content);
        }else {
            JSONObject body = new JSONObject();
            //密钥
            body.put("token",vlmToken);
            //API名
            body.put("api","InviteInGroupByLink");
            //机器人ID
            body.put("robot_wxid",content.getString("robot_wxid"));
            //群id
            body.put("group_wxid",systemParamUtil.querySystemParam("OFFICIALGROUP"));
            //好友ID
            body.put("friend_wxid",content.getString("from_wxid"));
            String result = HttpRequest.post(vlwUrl)
                    .header("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)")
                    .header("Content-Type", "application/json")
                    .body(body.toJSONString())
                    .execute().body();
            if (StringUtils.isEmpty(result)){
                log.info("邀请好友入群_直接拉接口调用失败");
            }else {
                JSONObject jsonObject = JSONObject.parseObject(result);
                if ("0".equals(jsonObject.getString("Code"))){
                    log.info("邀请好友入群_直接拉接口调用成功");
                }else {
                    log.info("邀请好友入群调用失败");
                    log.info("调用失败：{}",result);
                }
            }
        }
    }

    /**
     * 是否为管理员
     */
    public boolean isMaster(JSONObject content){
        //发送人
        String from_wxid = content.getString("from_wxid");
        //判断是否为管理员
        String masters = systemParamUtil.querySystemParam("WXMASTERS");
        if (StringUtils.isEmpty(masters)){
            sendTextMsg("尚未设置管理员，请先按照以下命令设置,多个请用@隔开", content);
            sendTextMsg("设置微信管理员 你的uid", content);
            return false;
        }else {
            if (masters.contains(from_wxid)){
                return true;
            }else {
                sendTextMsg("你不是管理员，无法执行该操作", content);
                return false;
            }
        }
    }
}
