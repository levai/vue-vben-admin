package com.vben.admin.core.enums;

/**
 * 基础错误码枚举
 *
 * @author vben
 */
public enum BaseErrMsg implements ErrMsg {

    /**
     * 操作成功
     */
    OK(0, "success"),

    /**
     * 参数校验不通过
     */
    ERR_MSG(400, "参数校验不通过"),

    /**
     * 业务异常
     */
    BUSINESS_MSG(500, "业务异常"),

    /**
     * 登录时效
     */
    API_LOGON_FAILURE(401, "登录已过期，请重新登录");

    private final int code;
    private final String message;

    BaseErrMsg(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
