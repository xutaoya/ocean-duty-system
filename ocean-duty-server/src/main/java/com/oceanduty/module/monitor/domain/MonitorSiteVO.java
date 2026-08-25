package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 网站监控视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorSiteVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 网站名称
     */
    private String siteName;

    /**
     * 网站地址
     */
    private String siteUrl;

    /**
     * 网站类型
     */
    private String siteType;

    /**
     * 状态 0异常 1正常 2警告
     */
    private Integer status;

    /**
     * HTTP响应状态码
     */
    private Integer httpStatus;

    /**
     * 响应时间(ms)
     */
    private Integer responseTime;

    /**
     * 最近检测时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 探测超时(ms)
     */
    private Integer timeoutMs;

    /**
     * 响应时间阈值(ms)
     */
    private Integer responseThreshold;
}
