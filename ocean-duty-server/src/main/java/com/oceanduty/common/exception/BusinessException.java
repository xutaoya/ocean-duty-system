package com.oceanduty.common.exception;

import com.oceanduty.common.constant.ResponseCodeConst;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResponseCodeConst codeConst) {
        super(codeConst.getMsg());
        this.code = codeConst.getCode();
    }

    public BusinessException(ResponseCodeConst codeConst, String message) {
        super(message);
        this.code = codeConst.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
