package com.oceanduty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能网格 FTP 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ocean-duty.monitor.grid-ftp")
public class SmartGridFtpProperties {

    private boolean enabled = true;

    private String host = "128.5.10.65";

    private int port = 21;

    private String username = "ag_duty_watcher";

    private String password = "";

    /**
     * 本地挂载根路径，如 /Volumes/128.5.10.65；配置后优先走本地文件系统
     */
    private String mountBase = "";

    private int connectTimeoutMs = 10000;

    private int dataTimeoutMs = 30000;
}
