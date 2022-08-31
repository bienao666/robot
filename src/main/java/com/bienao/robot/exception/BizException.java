package com.bienao.robot.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sedwt
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected String code;
    /**
     * 错误信息
     */
    protected String message;

    protected transient Object data;

    public BizException() {
        super();
    }

    public BizException(BaseErrorInfoInterface errorInfoInterface) {
        super(errorInfoInterface.getResultCode());
        code = errorInfoInterface.getResultCode();
        message = errorInfoInterface.getResultMsg();
        data = errorInfoInterface.getData();
    }

    public BizException(BaseErrorInfoInterface errorInfoInterface, Throwable cause) {
        super(errorInfoInterface.getResultCode(), cause);
        code = errorInfoInterface.getResultCode();
        message = errorInfoInterface.getResultMsg();
        data = errorInfoInterface.getData();
    }

    public BizException(String errorMsg) {
        super(errorMsg);
        message = errorMsg;
        data = null;
    }

    public BizException(String code, String errorMsg) {
        super(code);
        this.code = code;
        message = errorMsg;
        data = null;
    }

    public BizException(String code, String errorMsg, Object data) {
        super(code);
        this.code = code;
        message = errorMsg;
        this.data = data;
    }

    public BizException(String code, String errorMsg, Object data, Throwable cause) {
        super(code, cause);
        this.code = code;
        message = errorMsg;
        this.data = data;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
