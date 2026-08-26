package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据源返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorDatasourceVO {

    private Long id;

    private String dsName;

    private String dsType;

    private String host;

    private Integer port;

    private String databaseName;

    private String username;

    /**
     * 密码掩码，不回传明文
     */
    private String passwordMask;

    private String tableName;

    private Integer status;

    private LocalDateTime createTime;
}
