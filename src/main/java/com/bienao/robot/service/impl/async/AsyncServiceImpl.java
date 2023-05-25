package com.bienao.robot.service.impl.async;

import com.bienao.robot.entity.QlEntity;
import com.bienao.robot.entity.QlEnv;
import com.bienao.robot.service.async.AsyncService;
import com.bienao.robot.utils.WxpusherUtil;
import com.bienao.robot.utils.ql.QlUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AsyncServiceImpl implements AsyncService {

    @Autowired
    private QlUtil qlUtil;

    @Autowired
    private WxpusherUtil wxpusherUtil;


    @Override
    @Async("asyncServiceExecutor")
    public void updateWxpusherUid(List<QlEntity> qls, String ptPin, String code){
        String wxPusherUid = "";
        try {
            for (int i = 0; i < 2; i++) {
                wxPusherUid = wxpusherUtil.getWxpusherUid(code);
                if (StringUtils.isNotEmpty(wxPusherUid)) {
                    break;
                }
                try {
                    Thread.sleep(11 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isUpdate = false;
        if (StringUtils.isNotEmpty(wxPusherUid)){
            for (QlEntity ql : qls) {
                if (isUpdate) {
                    break;
                }
                List<QlEnv> envs = qlUtil.getEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken());
                for (QlEnv env : envs) {
                    if ("JD_COOKIE".equals(env.getName())) {
                        if (env.getValue().contains(ptPin)) {
                            //更新备注
                            String remarks = env.getRemarks();
                            if (StringUtils.isEmpty(remarks)) {
                                env.setRemarks(ptPin + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
                            } else {
                                String[] split = remarks.split("@@");
                                if (split.length == 1) {
                                    env.setRemarks(env.getRemarks() + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
                                }
                                if (split.length == 2) {
                                    env.setRemarks(split[0] + "@@" + System.currentTimeMillis() + "@@" + wxPusherUid);
                                }
                                if (split.length == 3) {
                                    env.setRemarks(split[0] + "@@" + System.currentTimeMillis() + "@@" + (StringUtils.isEmpty(wxPusherUid) ? split[2] : wxPusherUid));
                                }
                            }
                            if (qlUtil.updateEnvs(ql.getUrl(), ql.getTokenType(), ql.getToken(), env.getId(), env.getName(), env.getValue(), env.getRemarks())) {
                                isUpdate = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
