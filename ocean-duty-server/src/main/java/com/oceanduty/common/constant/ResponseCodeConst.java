package com.oceanduty.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局响应码常量
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeConst {

    SUCCESS(0, "操作成功"),
    ERROR_PARAM(10001, "参数错误"),
    ERROR_SYSTEM(10002, "系统异常"),
    NOT_LOGIN(10003, "未登录或登录已过期"),
    NO_PERMISSION(10004, "无操作权限"),
    NOT_FOUND(10005, "数据不存在");

    private final Integer code;
    private final String msg;
}
