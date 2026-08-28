package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 仪表盘「记录日志」按钮状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogSnapshotStatusVO {

    /** record / update / done */
    private String action;

    private String buttonLabel;

    private Boolean clickable;

    private Integer abnormalSiteCount;

    private Integer abnormalModuleCount;

    private Long lastLogId;

    private LocalDateTime lastLogTime;

    private String dutyDate;
}
