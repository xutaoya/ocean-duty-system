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

    private static final String CMS_HOST = "116.204.52.100";
    private static final int CMS_PORT = 3140;
    private static final String CMS_DATABASE = "hyyj";
    private static final String CMS_USERNAME = "ocean_work";

    private static final String GRID_HOST = "116.204.52.24";
    private static final int GRID_PORT = 3141;
    private static final String GRID_DATABASE = "hyyj";
    private static final String GRID_USERNAME = "ocean_work";

    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CredentialEncryptUtil credentialEncryptUtil;

    @Value("${ocean-duty.datasource.cms.initial-password:}")
    private String cmsInitialPassword;

    @Value("${ocean-duty.datasource.grid.initial-password:}")
    private String gridInitialPassword;

    @Value("${ocean-duty.datasource.typhoon-surge.initial-password:}")
    private String typhoonSurgeInitialPassword;

    @Value("${ocean-duty.datasource.typhoon-surge.ftp-password:}")
    private String typhoonSurgeFtpPassword;

    @Value("${ocean-duty.datasource.typhoon-surge.share-password:}")
    private String typhoonSurgeSharePassword;

    private static final String TYPHOON_SURGE_MYSQL_HOST = "116.204.52.200";
    private static final int TYPHOON_SURGE_MYSQL_PORT = 3140;
    private static final String TYPHOON_SURGE_PG_HOST = "128.5.2.164";
    private static final int TYPHOON_SURGE_PG_PORT = 5432;
    private static final String TYPHOON_SURGE_MYSQL_DATABASE = "center_site";
    private static final String TYPHOON_SURGE_PG_DATABASE = "web_surge";
    private static final String TYPHOON_SURGE_USERNAME = "ocean_work";

    @Override
    public void run(String... args) {
        if (StringUtils.hasText(cmsInitialPassword)) {
            String encryptedPassword = credentialEncryptUtil.encrypt(cmsInitialPassword);
            seedMysqlDatasource(1L, "中国海洋预报网CMS-灾害预警", "cms_forecast_alarm", encryptedPassword);
            seedMysqlDatasource(2L, "中国海洋预报网CMS-海区预报", "cms_forecast_area_firststage", encryptedPassword);
            seedMysqlDatasource(3L, "中国海洋预报网CMS-近岸预报", "cms_forecast_nearshoreseaarea", encryptedPassword);
            seedMysqlDatasource(4L, "中国海洋预报网CMS-月预报", "cms_article", encryptedPassword);
        } else {
            log.warn("未配置 CMS_DATASOURCE_PASSWORD，跳过默认 CMS 数据源初始化，请在「数据源管理」中手动添加");
        }

        if (StringUtils.hasText(gridInitialPassword)) {
            String encryptedPassword = credentialEncryptUtil.encrypt(gridInitialPassword);
            seedPostgresDatasource(5L, "中国海洋预报网PG-风", "app_wind_speed_grid", encryptedPassword);
            seedPostgresDatasource(6L, "中国海洋预报网PG-海浪", "app_wave_height_grid", encryptedPassword);
            seedPostgresDatasource(7L, "中国海洋预报网PG-海流", "app_current_speed_grid", encryptedPassword);
            seedPostgresDatasource(8L, "中国海洋预报网PG-海温", "app_sst_grid", encryptedPassword);
            seedPostgresDatasource(9L, "中国海洋预报网PG-风暴增水", "app_storm_tide_grid", encryptedPassword);
        } else {
            log.warn("未配置 GRID_DATASOURCE_PASSWORD，跳过默认智能网格数据源初始化，请在「数据源管理」中手动添加");
        }

        if (StringUtils.hasText(typhoonSurgeInitialPassword)) {
            String encryptedPassword = credentialEncryptUtil.encrypt(typhoonSurgeInitialPassword);
            seedTyphoonSurgeMysqlDatasource(10L, "台风风暴潮-网站库", "data_typhoon_surge_info", encryptedPassword);
            seedTyphoonSurgePostgresDatasource(11L, "台风风暴潮-PG库", "tb_typhoon_surge_info", encryptedPassword);
        } else {
            log.warn("未配置 TYPHOON_SURGE_DATASOURCE_PASSWORD，跳过台风风暴潮数据源初始化");
        }

        if (StringUtils.hasText(typhoonSurgeFtpPassword)) {
            String encryptedPassword = credentialEncryptUtil.encrypt(typhoonSurgeFtpPassword);
            seedTyphoonSurgeFtpDatasource(12L, "台风风暴潮-FTP", "/ty_surge/nc_maxsurge", encryptedPassword);
        }

        if (StringUtils.hasText(typhoonSurgeSharePassword)) {
            String encryptedPassword = credentialEncryptUtil.encrypt(typhoonSurgeSharePassword);
            seedTyphoonSurgeShareDatasource(13L, "台风风暴潮-原始文件共享", "ty_surge/result", encryptedPassword);
        }
    }

    private void seedMysqlDatasource(Long id, String name, String tableName, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("mysql")
                .host(CMS_HOST)
                .port(CMS_PORT)
                .databaseName(CMS_DATABASE)
                .username(CMS_USERNAME)
                .password(encryptedPassword)
                .tableName(tableName)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, tableName);
    }

    private void seedPostgresDatasource(Long id, String name, String tableName, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("postgresql")
                .host(GRID_HOST)
                .port(GRID_PORT)
                .databaseName(GRID_DATABASE)
                .username(GRID_USERNAME)
                .password(encryptedPassword)
                .tableName(tableName)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, tableName);
    }

    private void seedTyphoonSurgeMysqlDatasource(Long id, String name, String tableName, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("mysql")
                .host(TYPHOON_SURGE_MYSQL_HOST)
                .port(TYPHOON_SURGE_MYSQL_PORT)
                .databaseName(TYPHOON_SURGE_MYSQL_DATABASE)
                .username(TYPHOON_SURGE_USERNAME)
                .password(encryptedPassword)
                .tableName(tableName)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, tableName);
    }

    private void seedTyphoonSurgePostgresDatasource(Long id, String name, String tableName, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("postgresql")
                .host(TYPHOON_SURGE_PG_HOST)
                .port(TYPHOON_SURGE_PG_PORT)
                .databaseName(TYPHOON_SURGE_PG_DATABASE)
                .username(TYPHOON_SURGE_USERNAME)
                .password(encryptedPassword)
                .tableName(tableName)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, tableName);
    }

    private void seedTyphoonSurgeFtpDatasource(Long id, String name, String baseDir, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("ftp")
                .host("128.5.2.164")
                .port(21)
                .databaseName("")
                .username("surge_duty_watcher")
                .password(encryptedPassword)
                .tableName(baseDir)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, baseDir);
    }

    private void seedTyphoonSurgeShareDatasource(Long id, String name, String subDir, String encryptedPassword) {
        if (monitorDatasourceDao.selectById(id) != null) {
            return;
        }
        monitorDatasourceDao.insert(MonitorDatasourceEntity.builder()
                .id(id)
                .dsName(name)
                .dsType("smb")
                .host("172.16.30.160")
                .port(445)
                .databaseName("upload2surge")
                .username("upload2surge")
                .password(encryptedPassword)
                .tableName(subDir)
                .status(1)
                .build());
        log.info("已初始化数据源: {} ({})", name, subDir);
    }
}
