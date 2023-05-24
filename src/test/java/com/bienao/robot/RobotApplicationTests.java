package com.bienao.robot;

import com.bienao.robot.mapper.JetbrainsMapper;
import com.bienao.robot.service.ql.QlService;
import com.bienao.robot.service.weixin.WxService;
import com.bienao.robot.utils.ActivateJetBrainsUtil;
import com.bienao.robot.utils.ql.QlUtil;
import com.bienao.robot.utils.systemParam.SystemParamUtil;
import com.bienao.robot.utils.weixin.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


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

    @Autowired
    private JetbrainsMapper jetbrainsMapper;

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
        ActivateJetBrainsUtil.checkServer("http://jetbrains-lic.novx.org");
    }


}
