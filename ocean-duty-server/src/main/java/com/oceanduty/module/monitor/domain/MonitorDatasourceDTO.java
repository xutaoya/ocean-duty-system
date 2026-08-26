package com.oceanduty.module.monitor.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源新增/更新参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorDatasourceDTO {

    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    private String dsName;

    @NotBlank(message = "数据源类型不能为空")
    private String dsType;

    @NotBlank(message = "主机地址不能为空")
    private String host;

    @NotNull(message = "端口不能为空")
    private Integer port;

    @NotBlank(message = "数据库名不能为空")
    private String databaseName;

    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（更新时留空表示不修改）
     */
    private String password;

    @NotBlank(message = "数据表名不能为空")
    private String tableName;

    private Integer status;
}
