package com.bienao.robot.entity;

import cn.hutool.extra.spring.SpringUtil;
import com.bienao.robot.service.ql.WireService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TgBot extends TelegramLongPollingBot {
    //填你自己的token和username
    private String token;
    private String username;

    public TgBot() {
        this( new DefaultBotOptions());
    }

    public TgBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            Long chatId = message.getChatId();

            String text = message.getText();

            log.info("tg监听消息 chatId：{}->text：{}",chatId,text);

            if (StringUtils.isNotEmpty(text)){
                //处理消息
                if (text.contains("export ")){
                    ApplicationContext applicationContext = SpringUtil.getApplicationContext();
                    WireService wireService = applicationContext.getBean(WireService.class);
                    Result result = wireService.addActivity(text);
                    if ("200".equals(result.getCode())){
                        sendMsg("线报添加成功，可去后台线报清单查看详情",chatId);
                    }else {
                        sendMsg(result.getMessage(),chatId);
                    }
                }
            }
        }
    }

    //回复普通文本消息
    public void sendMsg(String text,Long chatId){
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId));
        response.setText(text);
        try {
            execute(response);
        } catch (TelegramApiException e) {
        }
    }
}
