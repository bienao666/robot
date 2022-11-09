package com.bienao.robot;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
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

    @Test
    public void test1() {
        JSONObject jsonObject = new JSONObject();
        JSONObject content = new JSONObject();
        content.put("msg","摸鱼");
        content.put("type",1);
        jsonObject.put("content",content);
        wxService.handleMessage(jsonObject);
    }

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

    /**
     * 京粉数据查询
     */
    /*@Test
    public void jingfen(){
        String cookie = "pt_key=AAJjWyaGADDjVy6B2y4LrjwVxU4Blq1QCRZgG5QefdMAACbqDJeLNthlOh9-sH8aA92rTIxo_SA;pt_pin=2022414474;";
        String date = DateUtil.formatDateTime(DateUtil.date());
        JSONObject param = new JSONObject();
        param.put("startDate",date);
        param.put("endDate",date);
        param.put("mediaId","");
        param.put("proCont","");
        param.put("promotionId","");
        param.put("sourceEmt","");
        param.put("pageNo",1);
        param.put("pageSize",20);
        JSONObject body = new JSONObject();
        body.put("funName","querySpreadEffectData");
        body.put("param",param.toJSONString());
        URLEncoder urlEncoder = URLEncoder.createDefault();
        String resStr = HttpRequest.get("https://api.m.jd.com/api?appid=unionpc&body=" + urlEncoder.encode(body.toJSONString(), Charset.defaultCharset()) + "&functionId=union_report&loginType=2")
                .header("authority", "api.m.jd.com")
                .header("origin", "https://union.jd.com")
                .header("referer", "https://union.jd.com/")
                .header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("cookie", cookie)
                .execute().body();
        System.out.println(resStr);
    }*/


    public static void main(String[] args) {
        String s = "19【赚钱大赢家】海量低价好物，新人享1分购噢！ https:/JO17oOLGjgmZcM复自这段话￥FAgee153c3%来【レσ\uD83C\uDD96特价】";
        JSONObject body = new JSONObject();
        body.put("code",s);
        String resStr = HttpRequest.post("https://api.m.jd.com/client.action?functionId=jComExchange")
                .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 11; zh-cn; KB2000 Build/RP1A.201005.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36 HeyTapBrowser/40.7.19.3 uuid/cddaa248eaf1933ddbe92e9bf4d72cb3")
                .body(body.toJSONString())
                .execute().body();
        System.out.println(resStr);
    }


}
