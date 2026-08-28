package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 记录快照结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogRecordResultVO {

    private Long logId;

    private String userName;

    private LocalDateTime dutyTime;

    private Integer abnormalSiteCount;

    private Integer abnormalModuleCount;

    private String action;
}
