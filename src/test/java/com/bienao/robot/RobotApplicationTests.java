package com.bienao.robot;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;
import com.bienao.robot.entity.QlEnv;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class RobotApplicationTests {

    @Autowired
    private SystemParamUtil systemParamUtil;

    @Autowired
    private WxService wxService;

    @Autowired
    private WeChatUtil weChatUtil;

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private QlService qlService;

    /*@Test
    public void test1() {
        JSONObject jsonObject = new JSONObject();
        JSONObject content = new JSONObject();
        content.put("msg","摸鱼");
        content.put("type",1);
        jsonObject.put("content",content);
        wxService.handleMessage(jsonObject);
    }*/

    /**
     * 青龙数据转发
     */
    /*@Test
    public void zhuanfa(){
        //原青龙
        String url1 = "";
        String clientID1 = "";
        String clientSecret1 = "";
        JSONObject tokenJson1 = qlUtil.getToken(url1, clientID1, clientSecret1);
        String token1 = tokenJson1.getString("");
        String tokenType1 = tokenJson1.getString("");
        List<QlEnv> envs = qlUtil.getEnvs(url1, tokenType1, token1);
        //目标青龙
        String url2 = "";
        String clientID2 = "";
        String clientSecret2 = "";
        JSONObject tokenJson2 = qlUtil.getToken(url2, clientID2, clientSecret2);
        String token2 = tokenJson2.getString("token");
        String tokenType2 = tokenJson2.getString("token_type");
        for (QlEnv env : envs) {
            qlUtil.addEnvs(url2, tokenType2, token2, env.getName(),env.getValue(),env.getRemarks());
        }
    }*/
    public static void main(String[] args) {
        String host = "http://jetbrains-lic.novx.org";
        try {
            /*Pattern pattern = Pattern.compile("http[s]?://");
            Matcher matcher = pattern.matcher(host);
            String protocol = "";
            if (matcher.find()) {
                protocol = matcher.group();
            }
            String hostWithoutProtocol = host.replace(protocol, "");*/

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
                if (jsonObject.getStr("responseCode").equals("OK")){
                    System.out.println(jsonObject.getStr("ticketProperties"));
                }else {
                    System.out.println(jsonObject.getStr("message"));
                }
            }
        } catch (Exception error) {
            System.out.printf("[x]%s --- %s%n", host, error.getMessage());
        }
    }


}
