package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 监控快照单项（写入 snapshot_json，便于导出与闭环分析）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyMonitorSnapshotItemVO {

    private String targetType;

    private Long targetId;

    private String targetKey;

    private String targetName;

    private String category;

    private String checkType;

    private Integer status;

    private String statusLabel;

    private Map<String, Object> detail;
}
