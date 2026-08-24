package com.oceanduty.handler;

import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseDTO.<Void>builder()
                .code(e.getCode())
                .msg(e.getMessage())
                .build();
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseDTO<Void> handleValidException(Exception e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return ResponseDTO.wrap(ResponseCodeConst.ERROR_PARAM);
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseDTO.wrap(ResponseCodeConst.ERROR_SYSTEM);
    }
}
