package com.bienao.robot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@MapperScan("com.bienao.robot.mapper")
@EnableScheduling //开启定时任务
@Validated
public class RobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotApplication.class, args);

        /*//梯子在自己电脑上就写127.0.0.1  软路由就写路由器的地址
        String proxyHost = "127.0.0.1";
        //端口根据实际情况填写，说明在上面，自己看
        int proxyPort = 7890;

        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setProxyHost(proxyHost);
        botOptions.setProxyPort(proxyPort);
        //注意一下这里，ProxyType是个枚举，看源码你就知道有NO_PROXY,HTTP,SOCKS4,SOCKS5;
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);

        DefaultBotSession defaultBotSession = new DefaultBotSession();
        defaultBotSession.setOptions(botOptions);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
            //需要代理
            TgBot bot = new TgBot(botOptions);
            telegramBotsApi.registerBot(bot);
//            //不需代理
//            TgBot bot2 = new TgBot();
//            telegramBotsApi.registerBot(bot2);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }*/
    }
}
