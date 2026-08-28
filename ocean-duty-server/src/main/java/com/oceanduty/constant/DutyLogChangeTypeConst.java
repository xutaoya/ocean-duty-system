package com.oceanduty.constant;

/**
 * 值班日志监控项变更类型
 */
public final class DutyLogChangeTypeConst {

    private DutyLogChangeTypeConst() {
    }

    /** 新出现异常 */
    public static final String NEW = "new";

    /** 异常状态变化（如警告→异常） */
    public static final String CHANGED = "changed";

    /** 持续异常（状态未变） */
    public static final String PERSISTENT = "persistent";

    /** 已恢复（本次快照中不再异常） */
    public static final String RECOVERED = "recovered";
}
