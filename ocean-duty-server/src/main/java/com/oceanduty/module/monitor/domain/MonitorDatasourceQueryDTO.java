package com.oceanduty.module.monitor.domain;

import com.oceanduty.common.domain.PageParamDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorDatasourceQueryDTO extends PageParamDTO {

    /**
     * 数据源名称
     */
    private String dsName;
}
