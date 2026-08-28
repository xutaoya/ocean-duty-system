package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 值班日志详情（含变更与异常事件）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogDetailVO {

    private Long id;

    private Long userId;

    private String userName;

    private LocalDateTime dutyTime;

    private LocalDate dutyDate;

    private String logSource;

    private String actionType;

    private Long previousLogId;

    private String siteStatus;

    private String moduleStatus;

    private String problem;

    private String solution;

    private LocalDateTime recoverTime;

    private String stateFingerprint;

    private Integer abnormalCount;

    private Integer newAbnormalCount;

    private Integer changedCount;

    private Integer recoveredCount;

    private DutyLogChangeSummaryVO changeSummary;

    private List<DutyLogItemVO> items;

    private List<DutyIncidentVO> incidents;
}
