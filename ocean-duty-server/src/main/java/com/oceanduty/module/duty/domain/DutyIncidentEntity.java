package com.oceanduty.module.duty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班异常事件（异常-恢复闭环）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("duty_incident")
public class DutyIncidentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String targetType;

    private Long targetId;

    private String targetKey;

    private String targetName;

    private String category;

    private String checkType;

    /** 异常首次出现日期（按天周期） */
    private LocalDate dutyDate;

    /** open / recovered */
    private String incidentStatus;

    private Integer firstStatus;

    private Integer lastStatus;

    private Long firstLogId;

    private Long lastLogId;

    private Long recoverLogId;

    private LocalDateTime firstSeenTime;

    /** 依据监控规则推算的故障时间 */
    private LocalDateTime firstFaultTime;

    private LocalDateTime lastSeenTime;

    private LocalDateTime recoveredTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
