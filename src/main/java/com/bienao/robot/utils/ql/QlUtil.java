package com.bienao.robot.utils.ql;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class QlUtil {

    //获取用户秘钥
    public JSONObject getToken(String url,String clientId,String clientSecret){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/auth/token?client_id=" + clientId +"&client_secret=" + clientSecret)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取用户秘钥失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙获取用户秘钥失败");
                return null;
            }
            return res.getJSONObject("data");
        } catch (Exception e) {
            log.info("青龙获取用户秘钥失败");
            return null;
        }
    }

    /**
     * 获取所有环境变量详情
     */
    public List<JSONObject> getEnvs(String url,String tokenType,String token){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取所有环境变量详情失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙获取所有环境变量详情失败");
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取所有环境变量详情失败");
            return null;
        }
    }

    /**
     * 添加环境变量
     */
    public boolean addEnvs(String url,String tokenType,String token,String name,String value,String remarks){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            ArrayList<JSONObject> body = new ArrayList<>();
            JSONObject env = new JSONObject();
            env.put("name",name);
            env.put("value",value);
            env.put("remarks",remarks);
            body.add(env);
            String resStr = HttpRequest.post(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(body))
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙添加环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙添加环境变量失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙添加环境变量失败");
            return false;
        }
    }

    /**
     * 更新环境变量
     */
    public boolean updateEnvs(String url,String tokenType,String token,String name,String value,String remarks){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            ArrayList<JSONObject> body = new ArrayList<>();
            JSONObject env = new JSONObject();
            env.put("name",name);
            env.put("value",value);
            env.put("remarks",remarks);
            env.put("id",0);
            body.add(env);
            String resStr = HttpRequest.post(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙更新环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙更新环境变量失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙更新环境变量失败");
            return false;
        }
    }

    /**
     * 删除环境变量
     */
    public boolean deleteEnvs(String url,String tokenType,String token,List<Integer> ids){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.delete(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙删除环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙删除环境变量失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙删除环境变量失败");
            return false;
        }
    }

    /**
     * 根据id获取环境变量
     */
    public JSONObject getEnvById(String url,String tokenType,String token,String id){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/envs/"+id)
                    .header("Authorization",tokenType + " " + token)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙查询环境变量失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙查询环境变量失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data);
        } catch (HttpException e) {
            log.info("青龙查询环境变量失败");
            return null;
        }
    }

    /**
     * 移动环境变量
     */
    public JSONObject moveEnv(String url,String tokenType,String token,String id,Integer fromIndex,Integer toIndex){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("fromIndex",fromIndex);
            body.put("toIndex",toIndex);
            String resStr = HttpRequest.get(url + "open/envs/"+id+"/move")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙移动环境变量失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙移动环境变量失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data);
        } catch (HttpException e) {
            log.info("青龙移动环境变量失败");
            return null;
        }
    }

    /**
     * 禁用环境变量
     */
    public boolean disableEnv(String url,String tokenType,String token,List<Integer> ids){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/envs/disable")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙禁用环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙禁用环境变量失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙禁用环境变量失败");
            return false;
        }
    }

    /**
     * 启用环境变量
     */
    public boolean enableEnv(String url,String tokenType,String token,List<Integer> ids){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/envs/enable")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙启用环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙启用环境变量失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙启用环境变量失败");
            return false;
        }
    }

    /**
     * 获取配置文件列表
     */
    public List<JSONObject> getFiles(String url,String tokenType,String token){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/configs/files")
                    .header("Authorization",tokenType + " " + token)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取配置文件列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙获取配置文件列表失败");
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取配置文件列表失败");
            return null;
        }
    }

    /**
     * 获取配置文件内容
     */
    public JSONObject getFile(String url,String tokenType,String token,String file){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/configs/"+file)
                    .header("Authorization",tokenType + " " + token)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取配置文件内容失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙获取配置文件内容失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data);
        } catch (HttpException e) {
            log.info("青龙获取配置文件内容失败");
            return null;
        }
    }

    /**
     * 保存配置文件
     */
    public JSONObject saveFile(String url,String tokenType,String token,String name,String content){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("name",name);
            body.put("content",content);
            String resStr = HttpRequest.get(url + "open/configs/save")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙保存配置文件失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙保存配置文件失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data);
        } catch (HttpException e) {
            log.info("青龙保存配置文件失败");
            return null;
        }
    }

    /**
     * 获取所有日志列表
     */
    public List<JSONObject> getLogs(String url, String tokenType, String token){
        if (url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/logs")
                    .header("Authorization",tokenType + " " + token)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取所有日志列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!res.getString("code").equals("200")){
                log.info("青龙获取所有日志列表失败");
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取所有日志列表失败");
            return null;
        }
    }
}
