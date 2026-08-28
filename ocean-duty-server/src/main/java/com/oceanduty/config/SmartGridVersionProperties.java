package com.oceanduty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能网格起报时间（MySQL pro_version_controller）配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ocean-duty.monitor.grid-version")
public class SmartGridVersionProperties {

    private long mysqlDatasourceId = 1L;

    private String controllerTable = "pro_version_controller";
}
