package com.vben.admin.core.enums;

/**
 * 错误码接口
 *
 * @author vben
 */
public interface ErrMsg {

    /**
     * 默认成功CODE
     */
    int DEFAULT_SUCCESS_CODE = 0;

    /**
     * 默认成功MESSAGE
     */
    String DEFAULT_SUCCESS_MESSAGE = "ok";

    /**
     * 错误码
     *
     * @return 错误码
     */
    int getCode();

    /**
     * 错误信息
     *
     * @return 错误信息
     */
    String getMessage();
}
