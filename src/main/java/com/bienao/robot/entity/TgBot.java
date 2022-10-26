package com.bienao.robot.entity;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.mapper.ForwardMapper;
import com.bienao.robot.mapper.GroupMapper;
import com.bienao.robot.service.ql.WireService;
import com.bienao.robot.utils.ForwardUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
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

import java.util.List;

@Slf4j
@Component
public class TgBot extends TelegramLongPollingBot {
    //填你自己的token和username
    private String token;
    private String username;

    public TgBot() {
        this(new DefaultBotOptions());
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

            log.info("tg监听消息 {}", JSONObject.toJSONString(message));

            //获取群号
            Long chatId = message.getChatId();
            String text = message.getText();
            log.info("tg监听消息 chatId：{}->text：{}", chatId, text);

            ApplicationContext applicationContext = SpringUtil.getApplicationContext();

            if (StringUtils.isNotEmpty(text)) {
                SystemParamUtil systemParamUtil = applicationContext.getBean(SystemParamUtil.class);
                String islistenwire = systemParamUtil.querySystemParam("ISLISTENWIRE");
                //处理消息
                if ("是".equals(islistenwire) && text.contains("export ")) {
                    WireService wireService = applicationContext.getBean(WireService.class);
                    Result result = wireService.addActivity(text);
                    if ("200".equals(result.getCode())) {
                        sendMsg("线报添加成功", chatId);
                    } else {
                        sendMsg(result.getMessage(), chatId);
                    }
                }
            }

            //记录群
            if (chatId != null) {
                GroupMapper groupMapper = applicationContext.getBean(GroupMapper.class);
                List<Group> groups = groupMapper.queryGroupByGroupId(String.valueOf(chatId));
                if (groups.size() == 0) {
                    Group group = new Group();
                    group.setGroupid(String.valueOf(chatId));
                    group.setGroupName(message.getChat().getTitle());
                    groupMapper.addGroup(group);
                }
            }

            //转发
            ForwardMapper forwardMapper = applicationContext.getBean(ForwardMapper.class);
            List<ForwardEntity> list = forwardMapper.queryForward(String.valueOf(chatId), null, null, null);
            if (list.size()>0){
                ForwardUtil forwardUtil = applicationContext.getBean(ForwardUtil.class);
                for (ForwardEntity forwardEntity : list) {
                    forwardUtil.forward(text,forwardEntity.getTo(),forwardEntity.getTotype());
                }
            }
        }
    }

    //回复普通文本消息
    public void sendMsg(String text, Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId));
        response.setText(text);
        try {
            execute(response);
        } catch (TelegramApiException e) {
        }
    }
}
