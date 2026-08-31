package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班日志视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 值班人员
     */
    private String userName;

    /**
     * 值班时间
     */
    private LocalDateTime dutyTime;

    /**
     * 网站状态摘要
     */
    private String siteStatus;

    /**
     * 模块状态摘要
     */
    private String moduleStatus;

    /**
     * 故障原因
     */
    private String problem;

    /**
     * 处理措施
     */
    private String solution;

    /**
     * 恢复时间
     */
    private LocalDateTime recoverTime;

    /**
     * 日志来源
     */
    private String logSource;

    /**
     * 动作类型 record / update / manual
     */
    private String actionType;

    /**
     * 值班日期
     */
    private LocalDate dutyDate;

    /**
     * 异常总数
     */
    private Integer abnormalCount;

    /**
     * 新异常数
     */
    private Integer newAbnormalCount;

    /**
     * 恢复数
     */
    private Integer recoveredCount;

    /**
     * 异常-恢复闭环摘要
     */
    private String closureSummary;
}
