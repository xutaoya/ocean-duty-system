package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 值班日志监控项明细 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogItemVO {

    private Long id;

    private String targetType;

    private Long targetId;

    private String targetKey;

    private String targetName;

    private String category;

    private String checkType;

    private Integer status;

    private Integer previousStatus;

    private String statusLabel;

    private String changeType;

    private String stateToken;

    private String detailJson;

    private LocalDateTime eventTime;

    private String eventTimeType;
}
