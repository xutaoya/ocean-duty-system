package com.oceanduty.common.domain;

import com.oceanduty.common.constant.ResponseCodeConst;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public static <T> ResponseDTO<T> succ() {
        return ResponseDTO.<T>builder()
                .code(ResponseCodeConst.SUCCESS.getCode())
                .msg(ResponseCodeConst.SUCCESS.getMsg())
                .build();
    }

    public static <T> ResponseDTO<T> succ(T data) {
        return ResponseDTO.<T>builder()
                .code(ResponseCodeConst.SUCCESS.getCode())
                .msg(ResponseCodeConst.SUCCESS.getMsg())
                .data(data)
                .build();
    }

    public static <T> ResponseDTO<T> wrap(ResponseCodeConst codeConst) {
        return ResponseDTO.<T>builder()
                .code(codeConst.getCode())
                .msg(codeConst.getMsg())
                .build();
    }

    public static <T> ResponseDTO<T> wrap(ResponseCodeConst codeConst, String msg) {
        return ResponseDTO.<T>builder()
                .code(codeConst.getCode())
                .msg(msg)
                .build();
    }
}
