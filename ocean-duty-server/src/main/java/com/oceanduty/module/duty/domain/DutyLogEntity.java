package com.oceanduty.module.duty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("duty_log")
public class DutyLogEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 值班人员
     */
    private String userName;

    /**
     * 值班人员 ID
     */
    private Long userId;

    /**
     * 值班时间
     */
    private LocalDateTime dutyTime;

    /**
     * 值班日期（按天周期）
     */
    private LocalDate dutyDate;

    /**
     * 网站状态摘要
     */
    private String siteStatus;

    /**
     * 模块状态摘要
     */
    private String moduleStatus;

    /**
     * 故障原因
     */
    private String problem;

    /**
     * 处理措施
     */
    private String solution;

    /**
     * 恢复时间
     */
    private LocalDateTime recoverTime;

    /**
     * 日志来源 manual / snapshot
     */
    private String logSource;

    /**
     * 动作类型 record / update / manual
     */
    private String actionType;

    /**
     * 上一条日志 ID（同日链式）
     */
    private Long previousLogId;

    /**
     * 异常状态指纹（用于判断是否需要更新日志）
     */
    private String stateFingerprint;

    /**
     * 变更摘要 JSON
     */
    private String changeSummary;

    /**
     * 当前异常总数
     */
    private Integer abnormalCount;

    /**
     * 新异常数
     */
    private Integer newAbnormalCount;

    /**
     * 状态变化数
     */
    private Integer changedCount;

    /**
     * 恢复数
     */
    private Integer recoveredCount;

    /**
     * 监控快照 JSON
     */
    private String snapshotJson;

    /**
     * 异常-恢复闭环摘要
     */
    private String closureSummary;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deletedFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
