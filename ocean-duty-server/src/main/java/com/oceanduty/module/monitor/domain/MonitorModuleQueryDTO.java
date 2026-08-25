package com.oceanduty.module.monitor.domain;

import com.oceanduty.common.domain.PageParamDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监控模块查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorModuleQueryDTO extends PageParamDTO {

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 模块分类
     */
    private String moduleCategory;

    /**
     * 模块分组
     */
    private String moduleGroup;
}
