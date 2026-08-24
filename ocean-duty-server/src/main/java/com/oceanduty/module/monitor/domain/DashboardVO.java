package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 监控仪表盘视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    /**
     * 异常网站列表
     */
    private List<MonitorSiteVO> abnormalSites;

    /**
     * 全部网站列表
     */
    private List<MonitorSiteVO> sites;

    /**
     * 模块监控列表
     */
    private List<MonitorModuleVO> modules;
}
