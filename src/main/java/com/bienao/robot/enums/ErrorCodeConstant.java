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

    /**
     *青龙修改失败
     */
    public static final String QINGLONG_DELETE_ERROR="30005";

    /**
     *接口调用失败
     */
    public static final String INTERFACE_CALL_ERROR="30005";

    /**
     *系统参数操作失败
     */
    public static final String SYSTEMPARAM_ERROR="30006";

    /**
     *大车头迁移失败
     */
    public static final String BIGHEAD_MOVE_ERROR="30007";

    /**
     * 数据库操作失败
     */
    public static final String DATABASE_OPERATE_ERROR="30008";

    /**
     * 服务异常
     */
    public static final String SERVICE_ERROR="30009";

    /**
     * telegram接口异常
     */
    public static final String TELEGRAM_ERROR="30010";

    /**
     * 未注册
     */
    public static final String NO_REGISTER_ERROR="30011";

    /**
     * 未登陆
     */
    public static final String NO_LOGIN_ERROR="30012";

    /**
     * 已过期
     */
    public static final String EXPIRE_ERROR="30013";

    /**
     * 注册异常
     */
    public static final String REGISTER_ERROR="30014";

    /**
     * 无token
     */
    public static final String NO_TOKEN_ERROR="30015";

    /**
     * token校验失败
     */
    public static final String TOKEN_CHECK_ERROR="30016";

    /**
     * 用户不存在
     */
    public static final String NO_USER_ERROR="30017";

    /**
     * token过期
     */
    public static final String TOKEN_EXPIRED_ERROR="30018";
}
