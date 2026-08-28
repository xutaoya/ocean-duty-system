package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 快照变更摘要（写入 duty_log.change_summary）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogChangeSummaryVO {

    private List<DutyLogChangeEntryVO> newAbnormals;

    private List<DutyLogChangeEntryVO> changed;

    private List<DutyLogChangeEntryVO> recovered;

    private List<DutyLogChangeEntryVO> persistent;
}
