package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模块关联的最新 CMS 警报详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsForecastAlarmDetailVO {

    /**
     * 模块ID
     */
    private Long moduleId;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 模块分组
     */
    private String moduleGroup;

    /**
     * 警报类型编码
     */
    private String alarmType;

    /**
     * 警报类型名称
     */
    private String alarmTypeName;

    /**
     * 警报名称
     */
    private String title;

    /**
     * 警报编号
     */
    private String code;

    /**
     * 发布时间
     */
    private LocalDateTime alarmDate;

    /**
     * 警报等级
     */
    private String level;

    /**
     * 警报图片
     */
    private String image;

    /**
     * 警报描述
     */
    private String description;

    /**
     * 防御指南
     */
    private String defenseGuide;

    /**
     * 标准说明
     */
    private String standard;

    /**
     * 扩展内容
     */
    private String content;
}
