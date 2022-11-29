package com.bienao.robot.Socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ZqdyjWebSocket/{token}")
@Component
@Slf4j
public class ZqdyjWebSocket {
    private Session session;
    private static int onlineCount = 0;
    private static Map<String, ZqdyjWebSocket> clients = new ConcurrentHashMap<String, ZqdyjWebSocket>();
    private String token;


    /**
     * 新建链接
     * @param token
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session){
        this.token = token;
        this.session=session;
        clients.put(this.token, this);
        addOnlineCount();
        log.info("【websocket消息】有新的连接，总数：{}",onlineCount);
    }

    /**
     * 断开链接
     */
    @OnClose
    public void onClose(){
        clients.remove(token);
        subOnlineCount();
        log.info("【websocket消息】连接断开，总数：{}",onlineCount);
    }

    /**
     * 接收消息
     * @param message
     */
    @OnMessage
    public void onMessage(String message){

        for (ZqdyjWebSocket item : clients.values()) {

            if (item.token.equals(token))

                item.session.getAsyncRemote().sendText(message);

        }

        log.info("【websocket消息】收到客户端发来的消息：{}",message);
    }

    /**
     * 发送到指定用户
     * @param message
     * @param ToUserId
     */
    public static void sendMessageToUserId(String message, String ToUserId){

        for (ZqdyjWebSocket item : clients.values()) {

            if (item.token.equals(ToUserId))

                item.session.getAsyncRemote().sendText(message);

        }

    }

    /**
     * 查询是否创建链接
     * @param ToUserId
     */
    public static int findToUserId(String ToUserId){
        int count = 0;

        for (ZqdyjWebSocket item : clients.values()) {

            if (item.token.equals(ToUserId)){
                count++;
            }
        }
        return count;
    }

    /**
     * 消息发送给所有用户
     * @param message
     */
    public void sendMessageAll(String message){

        for (ZqdyjWebSocket item : clients.values()) {

            try {
                item.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static synchronized int getOnlineCount() {

        return onlineCount;

    }

    public static synchronized void addOnlineCount() {

        ZqdyjWebSocket.onlineCount++;

    }

    public static synchronized void subOnlineCount() {

        ZqdyjWebSocket.onlineCount--;

    }

    public static synchronized Map<String, ZqdyjWebSocket> getClients() {

        return clients;

    }
}
