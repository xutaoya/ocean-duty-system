package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模块监控视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorModuleVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联网站ID
     */
    private Long siteId;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 模块页面地址
     */
    private String moduleUrl;

    /**
     * 模块分类
     */
    private String moduleCategory;

    /**
     * 模块分类名称
     */
    private String moduleCategoryName;

    /**
     * 模块分组
     */
    private String moduleGroup;

    /**
     * 检测方式
     */
    private String checkType;

    /**
     * 检测参数(JSON)
     */
    private String checkParam;

    /**
     * 数据更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 预期更新时间
     */
    private String expectedTime;

    /**
     * 状态 0异常 1正常 2警告
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 最新警报名称
     */
    private String alarmTitle;

    /**
     * 最新警报编号
     */
    private String alarmCode;

    /**
     * 最新警报等级
     */
    private String alarmLevel;

    /**
     * 最近检查时间
     */
    private LocalDateTime lastCheckTime;
}
