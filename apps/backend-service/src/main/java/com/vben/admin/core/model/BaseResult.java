package com.vben.admin.core.model;

import com.vben.admin.core.enums.BaseErrMsg;
import com.vben.admin.core.enums.ErrMsg;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 *
 * @author vben
 */
@Data
public class BaseResult<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码：0成功，其他为失败
     */
    public int code;

    /**
     * 成功为ok，其他为失败原因
     */
    public String message;

    /**
     * 数据结果集
     */
    public E data;

    public BaseResult() {
        this.code = ErrMsg.DEFAULT_SUCCESS_CODE;
        this.message = ErrMsg.DEFAULT_SUCCESS_MESSAGE;
    }

    public BaseResult(E data) {
        this();
        this.data = data;
    }

    public BaseResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResult(int code, String message, E data) {
        this(code, message);
        this.data = data;
    }

    public BaseResult(BaseResult<E> result) {
        this.code = result.getCode();
        this.message = result.getMessage();
        this.data = null;
    }

    public BaseResult(BaseResult<E> result, E data) {
        this.code = result.getCode();
        this.message = result.getMessage();
        this.data = data;
    }

    public BaseResult(BaseErrMsg errMsg, String message) {
        this.code = errMsg.getCode();
        this.message = message;
        this.data = null;
    }

    public BaseResult(BaseErrMsg errMsg, String message, E data) {
        this.code = errMsg.getCode();
        this.message = message;
        this.data = data;
    }
}
