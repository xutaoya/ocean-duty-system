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

    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CredentialEncryptUtil credentialEncryptUtil;

    @Value("${ocean-duty.datasource.cms.initial-password:}")
    private String initialPassword;

    @Override
    public void run(String... args) {
        if (monitorDatasourceDao.selectById(1L) != null) {
            return;
        }
        if (!StringUtils.hasText(initialPassword)) {
            log.warn("未配置 CMS_DATASOURCE_PASSWORD，跳过默认 CMS 数据源初始化，请在「数据源管理」中手动添加");
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(1L)
                .dsName("中国海洋预报网CMS")
                .dsType("mysql")
                .host("116.204.52.100")
                .port(3140)
                .databaseName("hyyj")
                .username("ocean_work")
                .password(credentialEncryptUtil.encrypt(initialPassword))
                .tableName("cms_forecast_alarm")
                .status(1)
                .build());
        log.info("已初始化默认 CMS 数据源（密码已加密存储）");
    }
}
