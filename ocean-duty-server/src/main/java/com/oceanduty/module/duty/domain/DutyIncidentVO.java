package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班异常事件 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyIncidentVO {

    private Long id;

    private String targetType;

    private Long targetId;

    private String targetKey;

    private String targetName;

    private String category;

    private String checkType;

    private LocalDate dutyDate;

    private String incidentStatus;

    private Integer firstStatus;

    private Integer lastStatus;

    private Long firstLogId;

    private Long lastLogId;

    private Long recoverLogId;

    private LocalDateTime firstSeenTime;

    private LocalDateTime firstFaultTime;

    private LocalDateTime lastSeenTime;

    private LocalDateTime recoveredTime;

    /** 异常-恢复闭环摘要（便于列表展示与导出） */
    private String lifecycleText;

    /** 本条日志中的事件角色 start / recover / ongoing / related */
    private String eventRole;

    private String eventRoleLabel;

    private String firstStatusLabel;

    private String lastStatusLabel;
}
