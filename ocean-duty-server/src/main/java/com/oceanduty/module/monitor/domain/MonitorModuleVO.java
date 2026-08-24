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
     * 最近检查时间
     */
    private LocalDateTime lastCheckTime;
}
