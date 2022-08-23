package com.bienao.robot.service.impl.jiankong;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.FunctionType;
import com.bienao.robot.Constants.jiankong.JKConstant;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.Group;
import com.bienao.robot.mapper.GroupMapper;
import com.bienao.robot.service.jiankong.Jiankong;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class JiankongImpl implements Jiankong {

    private HashSet<String> jiankong = JKConstant.set;

    private Cache<String, String> redis = WXConstant.redis;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private WeChatUtil weChatUtil;

    /**
     * 什么值得买-洋河
     */
    @Override
    public void jiankongsmzdmyh() {
        log.info("开始监控什么值得买-洋河。。。");
        try {
            Document doc = Jsoup.connect("https://search.smzdm.com/?c=home&s=%E5%A4%A9%E4%B9%8B%E8%93%9D+52%E5%BA%A6+480ML&order=time&v=b")
                    .data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
            Node content = doc.childNode(10).childNode(5).childNode(3).childNode(1).childNode(1).childNode(3).childNode(3).childNode(7);
            List<Node> nodes = content.childNodes();
            for (Node node : nodes) {
                if (StringUtils.isNotEmpty(node.toString().replaceAll(" ", ""))) {
                    Document goodsDoc = Jsoup.parse(node.toString());
                    try {
                        Node detail = goodsDoc.childNode(0).childNode(1).childNode(0).childNode(1).childNode(3);
                        // 09:08
                        try {
                            String dateStr = detail.childNode(7).childNode(3).childNode(3).childNode(0).toString();
                            //当天的数据
                            DateUtil.parse(dateStr, " HH:mm ");
                        } catch (Exception e) {
                            //不是当天的数据
                            continue;
                        }
                        String priceStr = detail.childNode(1).childNode(2).childNode(1).childNode(0).toString().trim();
                        int index = priceStr.indexOf("元");
                        String goodName = detail.childNode(1).childNode(2).attr("title");
                        double price = Double.parseDouble(priceStr.substring(0, index));
                        String suffix = priceStr.substring(index, priceStr.length());
                        String channel = detail.childNode(7).childNode(3).childNode(3).childNode(1).childNode(0).toString().trim();
                        String url = detail.childNode(7).childNode(3).childNode(1).childNode(1).childNode(1).attr("href");
                        if (goodName.contains("天之蓝") && price<600 && goodName.contains("2瓶")){
                            sendGoodsToGroup(goodName,price,suffix,channel,url);
                            sleep();
                        }
                        if (goodName.contains("梦之蓝") && price<1150 && goodName.contains("2瓶")){
                            sendGoodsToGroup(goodName,price,suffix,channel,url);
                            sleep();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info("监控洋河解析-异常：{}",e.getMessage());
                        log.info("监控洋河解析-内容：{}",goodsDoc.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("监控什么值得买-洋河结束。。。");
    }

    /**
     * 什么值得买-茅台
     */
    @Override
    public void jiankongsmzdmmt() {
        log.info("开始监控什么值得买-茅台。。。");
        try {
            Document doc = Jsoup.connect("https://search.smzdm.com/?c=home&s=%E8%8C%85%E5%8F%B0+%E9%A3%9E%E5%A4%A9+53&order=time&v=b")
                    .data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
            Node content = doc.childNode(10).childNode(5).childNode(3).childNode(1).childNode(1).childNode(3).childNode(3).childNode(7);
            List<Node> nodes = content.childNodes();
            for (Node node : nodes) {
                if (StringUtils.isNotEmpty(node.toString().replaceAll(" ", ""))) {
                    Document goodsDoc = Jsoup.parse(node.toString());
                    try {
                        Node detail = goodsDoc.childNode(0).childNode(1).childNode(0).childNode(1).childNode(3);
                        // 09:08
                        try {
                            String dateStr = detail.childNode(7).childNode(3).childNode(3).childNode(0).toString();
                            //不是当天的数据
                            DateUtil.parse(dateStr, " HH:mm ");
                        } catch (Exception e) {
                            continue;
                        }
                        String priceStr = detail.childNode(1).childNode(2).childNode(1).childNode(0).toString().trim();
                        int index = priceStr.indexOf("元");
                        String goodName = detail.childNode(1).childNode(2).attr("title");
                        double price = Double.parseDouble(priceStr.substring(0, index));
                        String suffix = priceStr.substring(index, priceStr.length());
                        String channel = detail.childNode(7).childNode(3).childNode(3).childNode(1).childNode(0).toString().trim();
                        String url = detail.childNode(7).childNode(3).childNode(1).childNode(1).childNode(1).attr("href");
                        if (goodName.contains("500ml") && price<1500){
                            sendGoodsToGroup(goodName,price,suffix,channel,url);
                            sleep();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info("监控茅台解析-异常：{}",e.getMessage());
                        log.info("监控茅台解析-内容：{}",goodsDoc.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("监控什么值得买-茅台结束。。。");
    }

    @Override
    public void resetSendList() {
        jiankong.clear();
        log.info("推送列表清空完成，当前列表个数为：{}",jiankong.size());
    }

    public void sendGoodsToGroup(String goodName, double price, String suffix, String channel, String url) {
        //判断当天是否发过，发过的当天不再发送
        int size1 = jiankong.size();
        jiankong.add(goodName + price + suffix + channel + url);
        int size2 = jiankong.size();
        if (size2 > size1) {
            String msg = "";
            msg += "商品:" + goodName + "\r\n";
            msg += "价格:" + price + suffix + "\r\n";
            msg += "渠道:" + channel + "\r\n";
            msg += "连接:" + url + "\r\n";
            List<Group> groups = groupMapper.queryGroupByFunctionType(FunctionType.mtyhjk);
            for (Group group : groups) {
                JSONObject content = new JSONObject();
                content.put("from_group", group.getGroupid());
                content.put("robot_wxid", systemParamUtil.querySystemParam("ROBORTWXID"));
                weChatUtil.sendTextMsg(msg, content);
            }
        }
    }

    public void sendGoodsToPerson(String goodName, double price, String suffix, String channel, String url) {
        //判断当天是否发过，发过的当天不再发送
        int size1 = jiankong.size();
        jiankong.add(goodName + price + suffix + channel + url);
        int size2 = jiankong.size();
        if (size2 > size1) {
            String msg = "";
            msg += "商品:" + goodName + "\r\n";
            msg += "价格:" + price + suffix + "\r\n";
            msg += "渠道:" + channel + "\r\n";
            msg += "连接:" + url + "\r\n";
            JSONObject content = new JSONObject();
            content.put("from_wxid","wxid_hwzi277usnv522");
            content.put("robot_wxid", "wxid_eyo76pqzpsvz12");
            weChatUtil.sendTextMsg(msg, content);
        }
    }

    /**
     * 休息
     */
    public void sleep(){
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
