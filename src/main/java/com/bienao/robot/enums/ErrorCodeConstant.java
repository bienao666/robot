package com.bienao.robot.enums;

/**
 * 2 * @Author: sedwt
 * 3 * @Date: 2020/10/15 14:47
 * 4
 */
public final class ErrorCodeConstant {
    private ErrorCodeConstant() {
    }

    /**
     *成功
     */
    public static final String SUCCESS="0";

    /**
     *认证失败
     */
    public static final String AUTHENTICATION_FAILED="20001";

    /**
     *权限不足
     */
    public static final String PERMISSION_ERROR="20002";

    /**
     *参数异常
     */
    public static final String PARAMETER_ERROR="30001";

    /**
     *青龙添加失败
     */
    public static final String QINGLONG_ADD_ERROR="30002";

    /**
     *青龙不存在
     */
    public static final String QINGLONG_NOT_EXSIT_ERROR="30003";

    /**
     *青龙已存在
     */
    public static final String QINGLONG_EXSIT_ERROR="30004";

    /**
     *青龙修改失败
     */
    public static final String QINGLONG_UPDATE_ERROR="30005";



}