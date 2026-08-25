package com.oceanduty.module.monitor.domain;

import com.oceanduty.common.domain.PageParamDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网站管理查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorSiteQueryDTO extends PageParamDTO {

    /**
     * 网站名称
     */
    private String siteName;

    /**
     * 网站类型
     */
    private String siteType;
}
