package com.bienao.robot.utils.weixin;


import com.bienao.robot.utils.SystemUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/*public class ItChat4jUtil implements IMsgHandlerFace {
    @Override
    public String textMsgHandle(BaseMsg baseMsg) {
        // String docFilePath = "D:/itchat4j/pic/1.jpg"; // 这里是需要发送的文件的路径
        if (!baseMsg.isGroupMsg()) { // 群消息不处理
            // String userId = msg.getString("FromUserName");
            // MessageTools.sendFileMsgByUserId(userId, docFilePath); // 发送文件
            // MessageTools.sendPicMsgByUserId(userId, docFilePath);
            String text = baseMsg.getText(); // 发送文本消息，也可调用MessageTools.sendFileMsgByUserId(userId,text);
            if ("exit".equals(text)) {
                WechatTools.logout();
            }
            if ("你好".equals(text)){
                WechatTools.sendMsgByUserName("你好",baseMsg.getFromUserName());
            }
            return text;
        }
        return null;
    }

    @Override
    public String picMsgHandle(BaseMsg baseMsg) {
        String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());// 这里使用收到图片的时间作为文件名
        String picPath = SystemUtil.getProjectPath()+"//itchat4j/pic" + File.separator + fileName + ".jpg"; // 调用此方法来保存图片
        DownloadTools.getDownloadFn(baseMsg, MsgTypeEnum.PIC.getType(), picPath); // 保存图片的路径
        return "图片保存成功";
    }

    @Override
    public String voiceMsgHandle(BaseMsg baseMsg) {
        String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String voicePath = SystemUtil.getProjectPath()+"//itchat4j/voice" + File.separator + fileName + ".mp3";
        DownloadTools.getDownloadFn(baseMsg, MsgTypeEnum.VOICE.getType(), voicePath);
        return "声音保存成功";
    }

    @Override
    public String viedoMsgHandle(BaseMsg baseMsg) {
        String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String viedoPath = SystemUtil.getProjectPath()+"//itchat4j/viedo" + File.separator + fileName + ".mp4";
        DownloadTools.getDownloadFn(baseMsg, MsgTypeEnum.VIEDO.getType(), viedoPath);
        return "视频保存成功";
    }

    @Override
    public String nameCardMsgHandle(BaseMsg baseMsg) {
        return "收到名片消息";
    }

    @Override
    public void sysMsgHandle(BaseMsg baseMsg) {
        String text = baseMsg.getContent();
    }

    @Override
    public String verifyAddFriendMsgHandle(BaseMsg baseMsg) {
        MessageTools.addFriend(baseMsg, true); // 同意好友请求，false为不接受好友请求
        RecommendInfo recommendInfo = baseMsg.getRecommendInfo();
        String nickName = recommendInfo.getNickName();
        String province = recommendInfo.getProvince();
        String city = recommendInfo.getCity();
        String text = "你好，来自" + province + city + "的" + nickName + "， 欢迎添加我为好友！";
        return text;
    }

    @Override
    public String mediaMsgHandle(BaseMsg baseMsg) {
        return null;
    }
}*/
