package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条监控项变更
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogChangeEntryVO {

    private String targetType;

    private Long targetId;

    private String targetKey;

    private String targetName;

    private String category;

    private String checkType;

    private Integer status;

    private Integer previousStatus;

    private String statusLabel;

    private String previousStatusLabel;

    private String changeType;

    private String stateToken;
}
