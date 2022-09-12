package com.bienao.robot.entity;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TgBot extends TelegramLongPollingBot {

    //填你自己的token和username
    private String token ="5595104960";
    private String username ="AAEcSHLiGL9OM6-XsNtCUQblpjziSoluyrk";

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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            Long chatId = message.getChatId();

            String text = message.getText();

            String[] split = text.split("=");
            if (split.length == 1){
                sendMsg("不处理该类指令",chatId);
                return;
            }

            String s = executeLinuxCmd(split[1]);
            sendMsg(s,chatId);
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


    public String executeLinuxCmd(String cmd) {
        System.out.println("执行命令[ " + cmd + " ]");
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(cmd);
            String line;
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer out = new StringBuffer();
            while ((line = stdoutReader.readLine()) != null ) {
                out.append(line+"\n");
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            process.destroy();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
