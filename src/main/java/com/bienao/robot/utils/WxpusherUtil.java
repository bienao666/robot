package com.bienao.robot.utils;

import cn.hutool.cache.Cache;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WxpusherUtil {

    private Cache<String, String> redis = WXConstant.redis;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WeChatUtil weChatUtil;

    /**
     * 获取wxpusher关注二维码
     * @param content
     * @return
     */
    public JSONObject getWxpusherCode(JSONObject content){
        String wxpusherToken = systemParamUtil.querySystemParam("WXPUSHERTOKEN");
        if (StringUtils.isEmpty(wxpusherToken)){
            return null;
        }
        JSONObject body = new JSONObject();
        body.put("appToken",wxpusherToken);
        body.put("extra","bienao");
        body.put("validTime",60);
        String resStr = HttpRequest.post("http://wxpusher.zjiecode.com/api/fun/create/qrcode")
                .body(body.toJSONString())
                .execute().body();
        if (StringUtils.isEmpty(resStr)){
            return null;
        }
        JSONObject res = JSONObject.parseObject(resStr);
        if (res.getInteger("code")==1000 && "处理成功".equals(res.getString("msg"))){
            JSONObject data = res.getJSONObject("data");
            String code = data.getString("code");
            String url = data.getString("url");
            if (content != null){
                weChatUtil.sendTextMsg("请在30s内扫描下方二维码：",content);
                weChatUtil.sendImageMsg(url,content);
                //发送人
                String from_wxid = content.getString("from_wxid");
                redis.put(from_wxid+"code",code,40 * 1000);
            }
            return data;
        }else {
            return null;
        }
    }

    /**
     * 获取wxpusheruid
     * @param content
     * @return
     */
    public String getWxpusherUid(JSONObject content){
        //发送人
        String from_wxid = content.getString("from_wxid");
        String code = redis.get(from_wxid + "code");
        String resStr = HttpRequest.get("https://wxpusher.zjiecode.com/api/fun/scan-qrcode-uid?code="+code)
                .timeout(3000)
                .execute().body();
        log.info("获取wxpusheruid接口返回：{}",resStr);
        if (StringUtils.isEmpty(resStr)){
            return null;
        }
        JSONObject res = JSONObject.parseObject(resStr);
        if (res.getInteger("code")==1000 && "处理成功".equals(res.getString("msg"))){
            return res.getString("data");
        }else {
            return null;
        }
    }

    /**
     * 获取wxpusheruid
     * @param code
     * @return
     */
    public String getWxpusherUid(String code){
        String resStr = HttpRequest.get("https://wxpusher.zjiecode.com/api/fun/scan-qrcode-uid?code="+code)
                .timeout(3000)
                .execute().body();
        log.info("获取wxpusheruid接口返回：{}",resStr);
        if (StringUtils.isEmpty(resStr)){
            return null;
        }
        JSONObject res = JSONObject.parseObject(resStr);
        if (res.getInteger("code")==1000 && "处理成功".equals(res.getString("msg"))){
            return res.getString("data");
        }else {
            return null;
        }
    }

    /**
     * 发送消息
     * @param content 消息内容
     * @param summary 消息摘要，显示在微信聊天页面或者模版消息卡片上，限制长度100，可以不传，不传默认截取content前面的内容。
     * @param contentType 内容类型 1表示文字  2表示html(只发送body标签内部的数据即可，不包括body标签) 3表示markdown
     * @param topicIds 发送目标的topicId，是一个数组！！！，也就是群发，使用uids单发的时候， 可以不传
     * @param uids 发送目标的UID，是一个数组。注意uids和topicIds可以同时填写，也可以只填写一个
     * @param url 原文链接，可选参数
     */
    public boolean sendMessage(String content, String summary, Integer contentType, List topicIds,List uids,String url){
        String wxpusherToken = systemParamUtil.querySystemParam("WXPUSHERTOKEN");
        if (StringUtils.isEmpty(wxpusherToken)){
            return false;
        }
        JSONObject body = new JSONObject();
        body.put("appToken",wxpusherToken);
        body.put("content",content);
        body.put("summary",summary);
        body.put("contentType",contentType);
        body.put("topicIds",topicIds);
        body.put("uids",uids);
        body.put("url",url);
        String resStr = HttpRequest.post("http://wxpusher.zjiecode.com/api/send/message")
                .body(body.toJSONString())
                .execute().body();
        if (StringUtils.isEmpty(resStr)){
            return false;
        }
        JSONObject res = JSONObject.parseObject(resStr);
        if (res.getInteger("code")==1000 && "处理成功".equals(res.getString("msg"))){
            return true;
        }else {
            return false;
        }
    }

    //发送京东登陆成功消息
    public boolean sendLogin(String ptPin,String uid,String ql){
        String content = "服务器："+ql+"\n"+"用户"+ptPin+"已上线";
        String summary = "robot通知：用户"+ptPin+"已上线";
        ArrayList<String> uids = new ArrayList<>();
        uids.add(uid);
        return sendMessage(content,summary,1,null,uids,"");
    }

    //发送京东红包通知
    public boolean sendJdRedPacketNotify(String content,String summary,String uid){
        ArrayList<String> uids = new ArrayList<>();
        uids.add(uid);
        return sendMessage(content,summary,1,null,uids,"");
    }
}
