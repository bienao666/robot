package com.bienao.robot.utils.ql;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bienao.robot.entity.QlCron;
import com.bienao.robot.entity.QlEnv;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class QlUtil {

    /**
     * 获取用户秘钥
     * @param url 青龙地址
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @return
     */
    public JSONObject getToken(String url,String clientId,String clientSecret){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/auth/token?client_id=" + clientId +"&client_secret=" + clientSecret)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取用户秘钥失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public List<QlEnv> getEnvs(String url,String tokenType,String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取所有环境变量详情失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取所有环境变量详情失败");
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data, QlEnv.class);
        } catch (HttpException e) {
            log.info("青龙获取所有环境变量详情失败");
            return null;
        }
    }

    /**
     * 添加环境变量
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param name 变量名
     * @param value 变量值
     * @param remarks 备注
     * @return
     */
    public QlEnv addEnvs(String url,String tokenType,String token,String name,String value,String remarks){
        if (!url.endsWith("/")){
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
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙添加环境变量失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙添加环境变量失败");
                return null;
            }
            String dataStr = res.getString("data");
            List<QlEnv> list = JSON.parseArray(dataStr, QlEnv.class);
            if (list.size()==0){
                return null;
            }else {
                return list.get(0);
            }
        } catch (HttpException e) {
            log.info("青龙添加环境变量失败");
            return null;
        }
    }

    /**
     * 更新环境变量
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param name 变量名
     * @param value 变量值
     * @param remarks 备注
     * @return
     */
    public boolean updateEnvs(String url,String tokenType,String token,Integer id,String name,String value,String remarks){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject env = new JSONObject();
            env.put("name",name);
            env.put("value",value);
            env.put("remarks",remarks);
            env.put("id",id);
            String resStr = HttpRequest.put(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .body(env.toString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙更新环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids 变量id
     * @return
     */
    public boolean deleteEnvs(String url,String tokenType,String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.delete(url + "open/envs")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙删除环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param id 变量id
     * @return
     */
    public JSONObject getEnvById(String url,String tokenType,String token,String id){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/envs/"+id)
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙查询环境变量失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param id 变量id
     * @param fromIndex 当前位置
     * @param toIndex 目标位置
     * @return
     */
    public JSONObject moveEnv(String url,String tokenType,String token,Integer id,Integer fromIndex,Integer toIndex){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("fromIndex",fromIndex);
            body.put("toIndex",toIndex);
            String resStr = HttpRequest.put(url + "open/envs/"+id+"/move")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙移动环境变量失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids 变量id
     * @return
     */
    public boolean disableEnv(String url,String tokenType,String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/envs/disable")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙禁用环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids 变量id
     * @return
     */
    public boolean enableEnv(String url,String tokenType,String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/envs/enable")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙启用环境变量失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public List<JSONObject> getFiles(String url,String tokenType,String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/configs/files")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取配置文件列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param file 配置文件名称
     * @return
     */
    public String getFile(String url,String tokenType,String token,String file){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/configs/"+file)
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取配置文件内容失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取配置文件内容失败");
                return null;
            }
            return res.getString("data");
        } catch (HttpException e) {
            log.info("青龙获取配置文件内容失败");
            return null;
        }
    }

    /**
     * 保存配置文件
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param name 配置文件名称
     * @param content 配置文件内容
     * @return
     */
    public boolean saveFile(String url,String tokenType,String token,String name,String content){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("name",name);
            body.put("content",content);
            String resStr = HttpRequest.post(url + "open/configs/save")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙保存配置文件失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙保存配置文件失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙保存配置文件失败");
            return false;
        }
    }

    /**
     * 获取所有任务日志列表
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public List<JSONObject> getLogs(String url, String tokenType, String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/logs")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取所有日志列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
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

    /**
     * 获取任务日志
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param dir 日志目录
     * @param file 日志文件名
     * @return
     */
    public JSONObject getLog(String url, String tokenType, String token,String dir,String file){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/logs/"+dir+"/"+file)
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取任务日志列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取任务日志列表失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取任务日志列表失败");
            return null;
        }
    }

    /**
     * 获取任务日志
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param file 日志文件名
     * @return
     */
    public JSONObject getLog(String url, String tokenType, String token,String file){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/logs/"+file)
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取任务日志列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取任务日志列表失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取任务日志列表失败");
            return null;
        }
    }

    /**
     * 获取所有任务详情
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public List<QlCron> getCrons(String url, String tokenType, String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/crons")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取所有任务详情失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取所有任务详情失败：{}",resStr);
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data, QlCron.class);
        } catch (HttpException e) {
            log.info("青龙获取所有任务详情失败",e);
            return null;
        }
    }

    /**
     * 添加任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param command 命令
     * @param schedule 定时(cron表达式)
     * @param name 任务名
     * @param labels 标签
     * @return
     */
    public boolean addCron(String url, String tokenType, String token,String command,String schedule,String name,String labels){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("command",command);
            body.put("schedule",schedule);
            body.put("name",name);
            body.put("labels",labels);
            String resStr = HttpRequest.post(url + "open/crons")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙添加任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙添加任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙添加任务失败");
            return false;
        }
    }

    /**
     * 更新任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param command 命令
     * @param schedule 定时(cron表达式)
     * @param name 任务名
     * @param labels 标签
     * @param id id
     * @return
     */
    public boolean updateCron(String url, String tokenType, String token,String command,String schedule,String name,String labels,Integer id){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("command",command);
            body.put("schedule",schedule);
            body.put("name",name);
            body.put("labels",labels);
            body.put("id",id);
            String resStr = HttpRequest.put(url + "open/crons")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙更新任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙更新任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙更新任务失败");
            return false;
        }
    }

    /**
     * 删除任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean deleteCron(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.delete(url + "open/crons")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙删除任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙删除任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙删除任务失败");
            return false;
        }
    }

    /**
     * 根据id获取定时任务详情
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param id id
     * @return
     */
    public JSONObject deleteCron(String url, String tokenType, String token,Integer id){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.delete(url + "open/crons/"+id)
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id获取定时任务详情失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id获取定时任务详情失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙根据id获取定时任务详情失败");
            return null;
        }
    }

    /**
     * 根据id运行任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean runCron(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/crons/run")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id运行任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id运行任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id运行任务失败");
            return false;
        }
    }

    /**
     * 根据id停止任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean stopCron(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/crons/stop")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id停止任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id停止任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id停止任务失败");
            return false;
        }
    }

    /**
     * 根据id添加标签
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param id id
     * @param label label
     * @return
     */
    public boolean addLabels(String url, String tokenType, String token,Integer id,String label){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("ids",id);
            body.put("label",label);
            String resStr = HttpRequest.post(url + "open/crons/labels")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id添加标签失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id添加标签失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id添加标签失败");
            return false;
        }
    }

    /**
     * 根据id删除标签
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param id id
     * @param label label
     * @return
     */
    public boolean deleteLabels(String url, String tokenType, String token,Integer id,String label){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("id",id);
            body.put("labels",label);
            String resStr = HttpRequest.delete(url + "open/crons/labels")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id删除标签失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id删除标签失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id删除标签失败");
            return false;
        }
    }

    /**
     * 根据id禁用定时任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean disableCrons(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/crons/disable")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id禁用定时任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id禁用定时任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id禁用定时任务失败");
            return false;
        }
    }

    /**
     * 根据id启用定时任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean enableCrons(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/crons/enable")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id启用定时任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id启用定时任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id启用定时任务失败");
            return false;
        }
    }

    /**
     * 根据id置顶定时任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean pinCrons(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/crons/pin")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id置顶定时任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id置顶定时任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id置顶定时任务失败");
            return false;
        }
    }

    /**
     * 根据id取消置顶定时任务
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean unpinCrons(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.put(url + "open/crons/unpin")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙根据id取消置顶定时任务失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙根据id取消置顶定时任务失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙根据id取消置顶定时任务失败");
            return false;
        }
    }

    /**
     * 获取所有脚本列表
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public List<JSONObject> getScripts(String url, String tokenType, String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/scripts/files")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取所有脚本列表失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取所有脚本列表失败");
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取所有脚本列表失败");
            return null;
        }
    }

    /**
     * 添加脚本
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param filename 脚本名称
     * @param path 脚本路径
     * @param content 脚本内容
     * @param originFilename 脚本原名称
     * @return
     */
    public boolean addScript(String url, String tokenType, String token,String filename,String path,String content,String originFilename){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("filename",filename);
            body.put("path",path);
            body.put("content",content);
            body.put("originFilename",originFilename);
            String resStr = HttpRequest.post(url + "open/scripts")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙添加脚本失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙添加脚本失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙添加脚本失败");
            return false;
        }
    }

    /**
     * 更新脚本
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param filename 脚本名称
     * @param path 脚本路径
     * @param content 脚本内容
     * @return
     */
    public boolean updateScript(String url, String tokenType, String token,String filename,String path,String content){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("filename",filename);
            body.put("path",path);
            body.put("content",content);
            String resStr = HttpRequest.put(url + "open/scripts")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙更新脚本失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙更新脚本失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙更新脚本失败");
            return false;
        }
    }

    /**
     * 删除脚本
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param filename 脚本名称
     * @param path 脚本路径
     * @return
     */
    public boolean deleteScript(String url, String tokenType, String token,String filename,String path){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("filename",filename);
            body.put("path",path);
            String resStr = HttpRequest.delete(url + "open/scripts")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙删除脚本失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙删除脚本失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙删除脚本失败");
            return false;
        }
    }

    /**
     * 下载脚本
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param filename 脚本名称
     * @return
     */
    public boolean downloadScript(String url, String tokenType, String token,String filename){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("filename",filename);
            String resStr = HttpRequest.post(url + "open/scripts/download")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙下载脚本失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙下载脚本失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙下载脚本失败");
            return false;
        }
    }

    /**
     * 运行脚本
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param filename 脚本名称
     * @param path 脚本路径
     * @return
     */
    public boolean runScript(String url, String tokenType, String token,String filename,String path){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("filename",filename);
            body.put("path",path);
            String resStr = HttpRequest.put(url + "open/scripts/run")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙运行脚本失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙运行脚本失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙运行脚本失败");
            return false;
        }
    }

    /**
     * 停止运行脚本
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param filename 脚本名称
     * @param path 脚本路径
     * @return
     */
    public boolean stopScript(String url, String tokenType, String token,String filename,String path){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("filename",filename);
            body.put("path",path);
            String resStr = HttpRequest.put(url + "open/scripts/stop")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙停止运行脚本失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙停止运行脚本失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙停止运行脚本失败");
            return false;
        }
    }

    /**
     * 获取已安装的依赖
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public List<JSONObject> getDependencies(String url, String tokenType, String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/dependencies")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙获取已安装的依赖失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙获取已安装的依赖失败");
                return null;
            }
            String data = res.getString("data");
            return JSON.parseArray(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙获取已安装的依赖失败");
            return null;
        }
    }

    /**
     * 添加依赖
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param name 依赖名称
     * @param type 依赖类型: 0 NodeJs 1 Python3 2 Linux
     * @param remark 备注
     * @return
     */
    public boolean addDependencies(String url, String tokenType, String token,String name,Integer type,String remark){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            ArrayList<JSONObject> body = new ArrayList<>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",name);
            jsonObject.put("type",type);
            jsonObject.put("remark",remark);
            body.add(jsonObject);
            String resStr = HttpRequest.post(url + "open/dependencies")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(body))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙添加依赖失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙添加依赖失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙添加依赖失败");
            return false;
        }
    }

    /**
     * 删除依赖
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean deleteDependencies(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.post(url + "open/dependencies")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙删除依赖失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙删除依赖失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙删除依赖失败");
            return false;
        }
    }

    /**
     * 暴力删除依赖
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean deleteForceDependencies(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.post(url + "open/dependencies/force")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙暴力删除依赖失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙暴力删除依赖失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙暴力删除依赖失败");
            return false;
        }
    }

    /**
     * 重装依赖
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param ids ids
     * @return
     */
    public boolean reinstallDependencies(String url, String tokenType, String token,List<Integer> ids){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.post(url + "open/dependencies/reinstall")
                    .header("Authorization",tokenType + " " + token)
                    .body(JSONObject.toJSONString(ids))
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙重装依赖失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙重装依赖失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙重装依赖失败");
            return false;
        }
    }

    /**
     * 查询日志删除频率
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @return
     */
    public JSONObject getRemoveLogTimes(String url, String tokenType, String token){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            String resStr = HttpRequest.get(url + "open/system/log/remove")
                    .header("Authorization",tokenType + " " + token)
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙查询日志删除频率失败");
                return null;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙查询日志删除频率失败");
                return null;
            }
            String data = res.getString("data");
            return JSONObject.parseObject(data,JSONObject.class);
        } catch (HttpException e) {
            log.info("青龙查询日志删除频率失败");
            return null;
        }
    }

    /**
     * 修改日志删除频率
     * @param url 青龙地址
     * @param tokenType token类型
     * @param token token
     * @param frequency 日志删除频率(天)
     * @return
     */
    public boolean updateRemoveLogTimes(String url, String tokenType, String token,Integer frequency){
        if (!url.endsWith("/")){
            url = url+"/";
        }
        try {
            JSONObject body = new JSONObject();
            body.put("frequency",frequency);
            String resStr = HttpRequest.put(url + "open/system/log/remove")
                    .header("Authorization",tokenType + " " + token)
                    .body(body.toJSONString())
                    .timeout(20000)
                    .execute().body();
            if (StringUtils.isEmpty(resStr)){
                log.info("青龙修改日志删除频率失败");
                return false;
            }
            JSONObject res = JSONObject.parseObject(resStr);
            if (!"200".equals(res.getString("code"))){
                log.info("青龙修改日志删除频率失败");
                return false;
            }
            return true;
        } catch (HttpException e) {
            log.info("青龙修改日志删除频率失败");
            return false;
        }
    }
}
