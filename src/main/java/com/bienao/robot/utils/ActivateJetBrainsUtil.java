package com.bienao.robot.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;

public class ActivateJetBrainsUtil {

    public static boolean checkServer(String host){
        try {
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0";
            String aliveUrl = "/rpc/ping.action?salt=1669702326321";
            String activeUrl = "/rpc/obtainTicket.action?" +
                    "machineId=cc696c3f-d41e-45b6-9d12-59e6d2250171&" +
                    "productCode=49c202d4-ac56-452b-bb84-735056242fb3&" +
                    "salt=1669702326321&" +
                    "userName=FuckYou&" +
                    "hostName=DESKTOP-FuckYou";

            String res = HttpRequest.get(host + aliveUrl)
                    .header("user-agent", userAgent)
                    .timeout(5000)
                    .execute().body();
            JSONObject jsonObject = XML.toJSONObject(res).getJSONObject("PingResponse");
            if (jsonObject.getStr("responseCode").equals("OK")){
                res = HttpRequest.get(host + activeUrl)
                        .header("user-agent", userAgent)
                        .timeout(5000)
                        .execute().body();
                jsonObject = XML.toJSONObject(res).getJSONObject("ObtainTicketResponse");
                if (jsonObject.getStr("responseCode").equals("OK") && jsonObject.getStr("ticketProperties").contains("tmetadata=")){
                    System.out.println(jsonObject.getStr("ticketProperties"));
                    return true;
                }else {
                    System.out.println(jsonObject.getStr("message"));
                }
            }
        } catch (Exception error) {
            System.out.printf("[x]%s --- %s%n", host, error.getMessage());
        }
        return false;
    }
}
