package com.oceanduty.module.duty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 值班日志监控项明细（可扩展 target_type）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("duty_log_item")
public class DutyLogItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long logId;

    /** site / module 等，后续可扩展 */
    private String targetType;

    private Long targetId;

    /** 全局唯一键，如 site:1、module:32 */
    private String targetKey;

    private String targetName;

    private String category;

    private String checkType;

    private Integer status;

    private String statusLabel;

    /** new / changed / persistent / recovered */
    private String changeType;

    private Integer previousStatus;

    /** 唯一状态标识 targetKey:status */
    private String stateToken;

    private String detailJson;

    /** 业务事件时间：故障/恢复（非按钮点击时间） */
    private LocalDateTime eventTime;

    /** fault / recover */
    private String eventTimeType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
