package com.oceanduty.module.monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模块数据源配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_datasource")
public class MonitorDatasourceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    private String dsName;

    /**
     * 数据源类型 mysql
     */
    private String dsType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 状态 0禁用 1正常
     */
    private Integer status;

    @TableLogic
    private Integer deletedFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
