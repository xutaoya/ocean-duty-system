package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CMS 灾害预警查询结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsForecastAlarmRecord {

    /**
     * 警报类型 wave/storm/bore/ice
     */
    private String type;

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
     * 警报图片地址
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
