package com.bienao.robot;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.Constants.weixin.WXConstant;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.entity.TgBot;
import com.bienao.robot.entity.WireEntity;
import com.bienao.robot.entity.WireKeyEntity;
import com.bienao.robot.mapper.WireKeyMapper;
import com.bienao.robot.mapper.WireMapper;
import com.bienao.robot.service.command.CommandService;
import com.bienao.robot.service.init.InitService;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.SystemUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Component
@Slf4j
public class AfterRunner implements ApplicationRunner {

    @Autowired
    private CommandService commandService;

    private Cache<String, String> redis = WXConstant.redis;

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private WireService wireService;

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private InitService initService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //登陆微信
//        initWeiXin();
        //初始化tgbot
        initializeTgBot();
        //初始化京东线报和命令
        initService.init();
        //启动成功通知
        weChatUtil.sendTextMsgToMaster("robot已开启，微信对接成功。。。");
        log.info("=================================================");
        log.info("===================web页面地址=====================");
        log.info("     http://127.0.0.1:8899/robot/index.html");
        log.info("=================================================");
    }

   /* private void initWeiXin() {
        String qrPath = SystemUtil.getProjectPath()+"//itchat4j//login"; // 保存登陆二维码图片的路径，这里需要在本地新建目录
        ItChat4jUtil msgHandler = new ItChat4jUtil(); // 实现IMsgHandlerFace接口的类
        Wechat wechat = new Wechat(msgHandler, qrPath); // 【注入】
        wechat.start(); // 启动服务，会在qrPath下生成一张二维码图片，扫描即可登陆，注意，二维码图片如果超过一定时间未扫描会过期，过期时会自动更新，所以你可能需要重新打开图片
    }*/

    public void initializeTgBot(){
        try {
            DefaultBotOptions botOptions = new DefaultBotOptions();
            List<SystemParam> systemParams = systemParamUtil.querySystemParams("TGPROXY");
            String tgProxy = systemParams.get(0).getValue();
            if (StringUtils.isNotEmpty(tgProxy) && tgProxy.contains(":")){
                String[] split = tgProxy.split(":");
                //梯子在自己电脑上就写127.0.0.1  软路由就写路由器的地址
                String proxyHost = split[0];
                //端口根据实际情况填写，说明在上面，自己看
                int proxyPort = Integer.parseInt(split[1]);
                botOptions.setProxyHost(proxyHost);
                botOptions.setProxyPort(proxyPort);
                //注意一下这里，ProxyType是个枚举，看源码你就知道有NO_PROXY,HTTP,SOCKS4,SOCKS5;
                botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
            }

            DefaultBotSession defaultBotSession = new DefaultBotSession();
            defaultBotSession.setOptions(botOptions);
            try {
                TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
                systemParams = systemParamUtil.querySystemParams("TGBOTTOKEN");
                String tgBotToken = systemParams.get(0).getValue();
                systemParams = systemParamUtil.querySystemParams("TGBOTUSERNAME");
                String tgBotUsername = systemParams.get(0).getValue();
                if (StringUtils.isNotEmpty(tgProxy) && tgProxy.contains(":")){
                    //需要代理
                    TgBot bot = new TgBot(botOptions);
                    bot.setToken(tgBotToken);
                    bot.setUsername(tgBotUsername);
                    telegramBotsApi.registerBot(bot);
                }else {
                    //不需代理
                    TgBot bot = new TgBot();
                    bot.setToken(tgBotToken);
                    bot.setUsername(tgBotUsername);
                    telegramBotsApi.registerBot(bot);
                }
                log.info("tgbot连接成功");
                log.info("开始监听tg信息。。。");
            } catch (TelegramApiException e) {
                log.info("tgbot连接失败，请检查配置");
            }
        } catch (NumberFormatException e) {
            log.info("tgbot连接失败，请检查配置");
        }
    }
}
