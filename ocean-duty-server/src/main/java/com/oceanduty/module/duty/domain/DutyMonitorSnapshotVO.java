package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控快照
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyMonitorSnapshotVO {

    private LocalDateTime capturedAt;

    private List<DutyMonitorSnapshotItemVO> items;
}
