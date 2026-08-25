package com.oceanduty.module.monitor.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控模块新增/更新参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorModuleDTO {

    /**
     * 主键ID（更新时必填）
     */
    private Long id;

    /**
     * 关联网站ID
     */
    @NotNull(message = "关联网站不能为空")
    private Long siteId;

    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    private String moduleName;

    /**
     * 模块页面地址
     */
    @NotBlank(message = "模块地址不能为空")
    private String moduleUrl;

    /**
     * 模块分类
     */
    @NotBlank(message = "模块分类不能为空")
    private String moduleCategory;

    /**
     * 模块分组
     */
    private String moduleGroup;

    /**
     * 检测方式
     */
    @NotBlank(message = "检测方式不能为空")
    private String checkType;

    /**
     * 检测参数(JSON)
     */
    @NotBlank(message = "检测参数不能为空")
    private String checkParam;

    /**
     * 预期更新时间(HH:mm)
     */
    @NotBlank(message = "预期更新时间不能为空")
    private String expectedTime;
}
