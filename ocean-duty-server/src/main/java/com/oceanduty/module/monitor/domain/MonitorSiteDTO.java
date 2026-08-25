package com.oceanduty.module.monitor.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站管理新增/更新参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorSiteDTO {

    /**
     * 主键ID（更新时必填）
     */
    private Long id;

    /**
     * 网站名称
     */
    @NotBlank(message = "网站名称不能为空")
    private String siteName;

    /**
     * 网站地址
     */
    @NotBlank(message = "网站地址不能为空")
    private String siteUrl;

    /**
     * 网站类型
     */
    @NotBlank(message = "网站类型不能为空")
    private String siteType;

    /**
     * 探测超时(ms)
     */
    @NotNull(message = "探测超时不能为空")
    @Min(value = 1000, message = "探测超时不能小于1000ms")
    private Integer timeoutMs;

    /**
     * 响应时间阈值(ms)
     */
    @NotNull(message = "响应阈值不能为空")
    @Min(value = 100, message = "响应阈值不能小于100ms")
    private Integer responseThreshold;
}
