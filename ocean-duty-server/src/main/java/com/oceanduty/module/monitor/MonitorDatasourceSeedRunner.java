package com.oceanduty.module.monitor;

import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.util.CredentialEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 模块数据源种子数据
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class MonitorDatasourceSeedRunner implements CommandLineRunner {

    private static final String DEFAULT_HOST = "116.204.52.100";
    private static final int DEFAULT_PORT = 3140;
    private static final String DEFAULT_DATABASE = "hyyj";
    private static final String DEFAULT_USERNAME = "ocean_work";

    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CredentialEncryptUtil credentialEncryptUtil;

    @Value("${ocean-duty.datasource.cms.initial-password:}")
    private String initialPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(initialPassword)) {
            log.warn("未配置 CMS_DATASOURCE_PASSWORD，跳过默认 CMS 数据源初始化，请在「数据源管理」中手动添加");
            return;
        }
        String encryptedPassword = credentialEncryptUtil.encrypt(initialPassword);
        seedDatasource(1L, "中国海洋预报网CMS-灾害预警", "cms_forecast_alarm", encryptedPassword);
        seedDatasource(2L, "中国海洋预报网CMS-海区预报", "cms_forecast_area_firststage", encryptedPassword);
        seedDatasource(3L, "中国海洋预报网CMS-近岸预报", "cms_forecast_nearshoreseaarea", encryptedPassword);
        seedDatasource(4L, "中国海洋预报网CMS-月预报", "cms_article", encryptedPassword);
    }

    private void seedDatasource(Long id, String name, String tableName, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("mysql")
                .host(DEFAULT_HOST)
                .port(DEFAULT_PORT)
                .databaseName(DEFAULT_DATABASE)
                .username(DEFAULT_USERNAME)
                .password(encryptedPassword)
                .tableName(tableName)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, tableName);
    }
}
