package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志时间线条目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogTimelineVO {

    private String targetType;

    private String targetKey;

    private String targetName;

    /** item / incident */
    private String source;

    private String changeType;

    private String eventRole;

    private String eventRoleLabel;

    private String description;

    private Integer status;

    private Integer previousStatus;

    private String statusLabel;
}
