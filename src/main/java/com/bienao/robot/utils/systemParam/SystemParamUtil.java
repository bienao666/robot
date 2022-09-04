package com.bienao.robot.utils.systemParam;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUnit;
import com.bienao.robot.Constants.systemParam.SysConstant;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.enums.ErrorCodeConstant;
import com.bienao.robot.mapper.SystemParamMapper;
import com.bienao.robot.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SystemParamUtil {

    @Autowired
    private SystemParamMapper systemParamMapper;

    private Cache<String, String> sysParamRedis = SysConstant.sysParamRedis;

    /**
     * 查询系统参数
     * @param code
     * @return
     */
    public String querySystemParam(String code){
        if (sysParamRedis.size()==0){
            List<SystemParam> systemParams = systemParamMapper.querySystems("","");
            for (SystemParam systemParam : systemParams) {
                sysParamRedis.put(systemParam.getCode(),systemParam.getValue(), DateUnit.DAY.getMillis());
            }
        }
        String value = sysParamRedis.get(code);
        if (StringUtils.isNotEmpty(value)){
            return value;
        }else {
            SystemParam systemParam = systemParamMapper.querySystem(code);
            if (systemParam != null){
                sysParamRedis.put(systemParam.getCode(),systemParam.getValue(), DateUnit.DAY.getMillis());
                return systemParam.getValue();
            }else {
                return null;
            }
        }
    }

    /**
     * 查询展示系统参数
     * @param key
     * @return
     */
    public List<SystemParam> queryShowSystems(String key){
        return systemParamMapper.queryShowSystems(key,key);
    }

    /**
     * 查询系统参数
     * @param key
     * @return
     */
    public List<SystemParam> querySystemParams(String key){
        return systemParamMapper.querySystems(key,key);
    }

    /**
     * 添加系统参数
     * @param systemParam
     * @return
     */
    public boolean addSystemParam(SystemParam systemParam){
        List<SystemParam> systemParams = systemParamMapper.querySystems(systemParam.getCode(),"");
        if (systemParams.size()==1){
            return false;
        }
        int i = systemParamMapper.addSystemParam(systemParam);
        if (i==1){
            sysParamRedis.put(systemParam.getCode(),systemParam.getValue(), DateUnit.DAY.getMillis());
            return true;
        }else {
            return false;
        }
    }

    /**
     * 修改系统参数
     * @param code
     * @param value
     * @return
     */
    public boolean updateSystemParam(String code,String codeName,String value){
        SystemParam systemParam = new SystemParam();
        systemParam.setCode(code);
        systemParam.setCodeName(codeName);
        systemParam.setValue(value);
        systemParam.setUpdatedTime(new Date());
        int i = systemParamMapper.updateSystemParam(systemParam);
        if (i==1){
            sysParamRedis.put(code,value);
            return true;
        }else {
            return false;
        }
    }

    /**
     * 删除系统参数
     * @param ids
     * @return
     */
    public Result deleteSystemParams(List<Integer> ids){
        int i = systemParamMapper.deleteSystemParams(ids);
        if (i==0){
            return Result.error(ErrorCodeConstant.QINGLONG_DELETE_ERROR,"系统参数删除异常");
        }else {
            return Result.success("删除成功");
        }
    }

}
