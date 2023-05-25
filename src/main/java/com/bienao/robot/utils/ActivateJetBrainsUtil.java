package com.bienao.robot.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
                    .timeout(3000)
                    .execute().body();
            JSONObject jsonObject = XML.toJSONObject(res).getJSONObject("PingResponse");
            if (jsonObject.getStr("responseCode").equals("OK")){
                res = HttpRequest.get(host + activeUrl)
                        .header("user-agent", userAgent)
                        .timeout(3000)
                        .execute().body();
                jsonObject = XML.toJSONObject(res).getJSONObject("ObtainTicketResponse");
                if (jsonObject.getStr("responseCode").equals("OK") && jsonObject.getStr("ticketProperties").contains("metadata=")){
                    return true;
                }else {
                    log.info("激活失败返回message：{}",jsonObject.getStr("message"));
                    log.info("激活失败返回详情：{}",jsonObject.toString());
                }
            }
        } catch (Exception error) {
            log.info(error.getMessage());
        }
        return false;
    }
}
