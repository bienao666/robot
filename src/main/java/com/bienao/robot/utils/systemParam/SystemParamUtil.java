package com.bienao.robot.utils.systemParam;

import cn.hutool.cache.Cache;
import cn.hutool.core.date.DateUnit;
import com.bienao.robot.Constants.systemParam.SysConstant;
import com.bienao.robot.entity.SystemParam;
import com.bienao.robot.mapper.SystemParamMapper;
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
            List<SystemParam> systemParams = systemParamMapper.querySystems();
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
     * 添加系统参数
     * @param systemParam
     * @return
     */
    public boolean addSystemParam(SystemParam systemParam){
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
        SystemParam systemParam = systemParamMapper.querySystem(code);
        if (systemParam==null){
            systemParam = new SystemParam();
            systemParam.setCode(code);
            systemParam.setCodeName(codeName);
            systemParam.setValue(value);
            return addSystemParam(systemParam);
        }else {
            systemParam.setValue(value);
            systemParam.setUpdatedTime(new Date());
            int i = systemParamMapper.updateSystemParam(systemParam);
            if (i==1){
                sysParamRedis.put(systemParam.getCode(),systemParam.getValue(), DateUnit.DAY.getMillis());
                return true;
            }else {
                return false;
            }
        }
    }

}
