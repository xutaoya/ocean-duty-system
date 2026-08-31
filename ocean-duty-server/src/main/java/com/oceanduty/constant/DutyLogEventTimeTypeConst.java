package com.oceanduty.constant;

/**
 * 值班日志业务事件时间类型
 */
public final class DutyLogEventTimeTypeConst {

    private DutyLogEventTimeTypeConst() {
    }

    /** 故障发生时间（依据监控规则推算） */
    public static final String FAULT = "fault";

    /** 恢复时间（依据数据源更新时间） */
    public static final String RECOVER = "recover";
}
