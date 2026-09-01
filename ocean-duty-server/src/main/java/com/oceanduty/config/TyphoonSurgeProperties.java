package com.oceanduty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 台风风暴潮 FTP / 共享目录配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ocean-duty.monitor.typhoon-surge")
public class TyphoonSurgeProperties {

    private Ftp ftp = new Ftp();

    private Share share = new Share();

    @Data
    public static class Ftp {

        private boolean enabled = true;

        private String host = "128.5.2.164";

        private int port = 21;

        private String username = "surge_duty_watcher";

        private String password = "";

        /** FTP 目录，如 /ty_surge/nc_maxsurge */
        private String baseDir = "/ty_surge/nc_maxsurge";

        /** 本地挂载根路径，配置后优先走本地文件系统 */
        private String mountBase = "";

        private int connectTimeoutMs = 10000;

        private int dataTimeoutMs = 30000;
    }

    @Data
    public static class Share {

        private boolean enabled = true;

        /** 共享目录本地挂载根路径，如 /Volumes/upload2surge */
        private String mountBase = "";

        /** 扫描子目录，如 ty_surge/result */
        private String subDir = "ty_surge/result";
    }
}
