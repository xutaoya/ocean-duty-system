package com.oceanduty.constant;

/**
 * 值班日志动作类型
 */
public final class DutyLogActionTypeConst {

    private DutyLogActionTypeConst() {
    }

    /** 当天首次自动记录 */
    public static final String RECORD = "record";

    /** 当天状态变化后更新 */
    public static final String UPDATE = "update";

    /** 手工填写 */
    public static final String MANUAL = "manual";
}
