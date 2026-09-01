package com.oceanduty.config;

import com.oceanduty.util.CredentialEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库结构增量迁移
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final CredentialEncryptUtil credentialEncryptUtil;

    @Value("${ocean-duty.datasource.grid.initial-password:}")
    private String gridInitialPassword;

    @Value("${ocean-duty.datasource.typhoon-surge.initial-password:}")
    private String typhoonSurgeInitialPassword;

    @Value("${ocean-duty.datasource.typhoon-surge.ftp-password:}")
    private String typhoonSurgeFtpPassword;

    @Value("${ocean-duty.datasource.typhoon-surge.share-password:}")
    private String typhoonSurgeSharePassword;

    @Override
    public void run(String... args) {
        Set<String> moduleColumns = loadTableColumns("monitor_module");
        addColumnIfMissing("monitor_module", moduleColumns, "module_category", "VARCHAR(50) NOT NULL DEFAULT 'disaster_warning'");
        addColumnIfMissing("monitor_module", moduleColumns, "module_group", "VARCHAR(50)");
        addColumnIfMissing("monitor_module", moduleColumns, "check_type", "VARCHAR(50) NOT NULL DEFAULT 'WARN_HISTORY'");
        addColumnIfMissing("monitor_module", moduleColumns, "check_param", "VARCHAR(500)");
        addColumnIfMissing("monitor_module", moduleColumns, "alarm_title", "VARCHAR(200)");
        addColumnIfMissing("monitor_module", moduleColumns, "alarm_code", "VARCHAR(100)");
        addColumnIfMissing("monitor_module", moduleColumns, "alarm_level", "VARCHAR(50)");

        Set<String> siteColumns = loadTableColumns("monitor_site");
        addColumnIfMissing("monitor_site", siteColumns, "timeout_ms", "INTEGER NOT NULL DEFAULT 10000");
        addColumnIfMissing("monitor_site", siteColumns, "response_threshold", "INTEGER NOT NULL DEFAULT 3000");
        syncDefaultSiteUrls();
        seedDefaultUsers();
        migratePlainPasswords();
        migrateDatasourcePasswords();
        seedCmsEnvDatasources();
        seedGridDatasources();
        seedTyphoonSurgeDatasources();
        seedTyphoonSurgeModule();
        migrateEnvModuleCheckParams();
        removeLegacyNmefcModules();
        fixTodayPublishedModuleStatus();
        fixMonthlyPublishedModuleStatus();
        renameStormTideModule();
        migrateDutyLogSnapshot();
    }

    private void migrateDutyLogSnapshot() {
        Set<String> dutyLogColumns = loadTableColumns("duty_log");
        addColumnIfMissing("duty_log", dutyLogColumns, "user_id", "INTEGER");
        addColumnIfMissing("duty_log", dutyLogColumns, "duty_date", "DATE");
        addColumnIfMissing("duty_log", dutyLogColumns, "log_source", "VARCHAR(20) DEFAULT 'manual'");
        addColumnIfMissing("duty_log", dutyLogColumns, "action_type", "VARCHAR(20)");
        addColumnIfMissing("duty_log", dutyLogColumns, "previous_log_id", "INTEGER");
        addColumnIfMissing("duty_log", dutyLogColumns, "state_fingerprint", "VARCHAR(500)");
        addColumnIfMissing("duty_log", dutyLogColumns, "change_summary", "TEXT");
        addColumnIfMissing("duty_log", dutyLogColumns, "abnormal_count", "INTEGER DEFAULT 0");
        addColumnIfMissing("duty_log", dutyLogColumns, "new_abnormal_count", "INTEGER DEFAULT 0");
        addColumnIfMissing("duty_log", dutyLogColumns, "changed_count", "INTEGER DEFAULT 0");
        addColumnIfMissing("duty_log", dutyLogColumns, "recovered_count", "INTEGER DEFAULT 0");
        addColumnIfMissing("duty_log", dutyLogColumns, "snapshot_json", "TEXT");
        addColumnIfMissing("duty_log", dutyLogColumns, "closure_summary", "TEXT");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_duty_log_duty_date ON duty_log(duty_date)");

        ensureDutyLogItemTable();
        ensureDutyIncidentTable();
    }

    private void ensureDutyLogItemTable() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'duty_log_item'", Integer.class);
        if (tableCount == null || tableCount == 0) {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS duty_log_item (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        log_id          INTEGER      NOT NULL,
                        target_type     VARCHAR(20)  NOT NULL,
                        target_id       INTEGER      NOT NULL,
                        target_key      VARCHAR(100) NOT NULL,
                        target_name     VARCHAR(200) NOT NULL,
                        category        VARCHAR(50),
                        check_type      VARCHAR(50),
                        status          TINYINT      NOT NULL,
                        status_label    VARCHAR(20),
                        change_type     VARCHAR(20),
                        previous_status TINYINT,
                        state_token     VARCHAR(120),
                        detail_json     TEXT,
                        create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            log.info("已创建 duty_log_item 表");
        }
        Set<String> itemColumns = loadTableColumns("duty_log_item");
        addColumnIfMissing("duty_log_item", itemColumns, "change_type", "VARCHAR(20)");
        addColumnIfMissing("duty_log_item", itemColumns, "previous_status", "TINYINT");
        addColumnIfMissing("duty_log_item", itemColumns, "state_token", "VARCHAR(120)");
        addColumnIfMissing("duty_log_item", itemColumns, "event_time", "DATETIME");
        addColumnIfMissing("duty_log_item", itemColumns, "event_time_type", "VARCHAR(20)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_duty_log_item_log_id ON duty_log_item(log_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_duty_log_item_target ON duty_log_item(target_type, target_id)");
    }

    private void ensureDutyIncidentTable() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'duty_incident'", Integer.class);
        if (tableCount == null || tableCount == 0) {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS duty_incident (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        target_type     VARCHAR(20)  NOT NULL,
                        target_id       INTEGER      NOT NULL,
                        target_key      VARCHAR(100) NOT NULL,
                        target_name     VARCHAR(200) NOT NULL,
                        category        VARCHAR(50),
                        check_type      VARCHAR(50),
                        duty_date       DATE         NOT NULL,
                        incident_status VARCHAR(20)  NOT NULL,
                        first_status    TINYINT      NOT NULL,
                        last_status     TINYINT,
                        first_log_id    INTEGER      NOT NULL,
                        last_log_id     INTEGER,
                        recover_log_id  INTEGER,
                        first_seen_time DATETIME     NOT NULL,
                        first_fault_time DATETIME,
                        last_seen_time  DATETIME,
                        recovered_time  DATETIME,
                        create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_duty_incident_duty_date ON duty_incident(duty_date)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_duty_incident_target ON duty_incident(target_key, incident_status)");
            log.info("已创建 duty_incident 表");
            return;
        }
        Set<String> incidentColumns = loadTableColumns("duty_incident");
        addColumnIfMissing("duty_incident", incidentColumns, "first_fault_time", "DATETIME");
    }

    /**
     * 天文潮模块更名为风暴增水
     */
    private void renameStormTideModule() {
        int moduleUpdated = jdbcTemplate.update(
                "UPDATE monitor_module SET module_name = '风暴增水' "
                        + "WHERE id = 35 AND deleted_flag = 0 AND module_name = '天文潮'");
        if (moduleUpdated > 0) {
            log.info("已将模块「天文潮」更名为「风暴增水」");
        }
        int datasourceUpdated = jdbcTemplate.update(
                "UPDATE monitor_datasource SET ds_name = '中国海洋预报网PG-风暴增水' "
                        + "WHERE id = 9 AND deleted_flag = 0 AND ds_name = '中国海洋预报网PG-天文潮'");
        if (datasourceUpdated > 0) {
            log.info("已将数据源「中国海洋预报网PG-天文潮」更名为「中国海洋预报网PG-风暴增水」");
        }
    }

    /**
     * 当天已有发布记录的模块修正为正常，并清理过期备注
     */
    private void fixTodayPublishedModuleStatus() {
        int updated = jdbcTemplate.update(
                "UPDATE monitor_module SET status = 1, remark = NULL "
                        + "WHERE deleted_flag = 0 AND data_update_time IS NOT NULL "
                        + "AND date(data_update_time) = date('now', 'localtime')");
        if (updated > 0) {
            log.info("已修正 {} 条当日已发布模块的状态为正常", updated);
        }
    }

    /**
     * 标题已归属当月的月更模块修正为正常（月报常在月末发布）
     */
    private void fixMonthlyPublishedModuleStatus() {
        int updated = jdbcTemplate.update(
                "UPDATE monitor_module SET status = 1, remark = NULL "
                        + "WHERE deleted_flag = 0 AND status = 0 "
                        + "AND check_param LIKE '%\"scheduleType\":\"monthly\"%' "
                        + "AND alarm_title IS NOT NULL "
                        + "AND alarm_title LIKE '%' || strftime('%Y', 'now', 'localtime') || '年' "
                        + "|| CAST(CAST(strftime('%m', 'now', 'localtime') AS INTEGER) AS TEXT) || '月%'");
        if (updated > 0) {
            log.info("已修正 {} 条当月月报模块的状态为正常", updated);
        }
    }

    /**
     * 删除国家海洋预报中心相关历史模块（首页已不再使用）
     */
    private void removeLegacyNmefcModules() {
        int updated = jdbcTemplate.update(
                "UPDATE monitor_module SET deleted_flag = 1 WHERE id <= 23 AND deleted_flag = 0");
        if (updated > 0) {
            log.info("已删除 {} 条未使用的历史模块数据", updated);
        }
    }

    /**
     * 从已有 CMS 数据源复制连接信息，补全环境预报相关数据表
     */
    private void seedCmsEnvDatasources() {
        Integer baseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM monitor_datasource WHERE id = 1 AND deleted_flag = 0", Integer.class);
        if (baseCount == null || baseCount == 0) {
            return;
        }
        jdbcTemplate.update(
                "UPDATE monitor_datasource SET ds_name = ? WHERE id = 1 AND ds_name = ?",
                "中国海洋预报网CMS-灾害预警", "中国海洋预报网CMS");
        insertFromBase(2L, "中国海洋预报网CMS-海区预报", "cms_forecast_area_firststage");
        insertFromBase(3L, "中国海洋预报网CMS-近岸预报", "cms_forecast_nearshoreseaarea");
        insertFromBase(4L, "中国海洋预报网CMS-月预报", "cms_article");
    }

    private void insertFromBase(long id, String name, String tableName) {
        int inserted = jdbcTemplate.update(
                "INSERT INTO monitor_datasource (id, ds_name, ds_type, host, port, database_name, username, password, table_name, status, deleted_flag) "
                        + "SELECT ?, ?, ds_type, host, port, database_name, username, password, ?, 1, 0 "
                        + "FROM monitor_datasource WHERE id = 1 AND deleted_flag = 0 "
                        + "AND NOT EXISTS (SELECT 1 FROM monitor_datasource t WHERE t.id = ? AND t.deleted_flag = 0)",
                id, name, tableName, id);
        if (inserted > 0) {
            log.info("已补全数据源: {} ({})", name, tableName);
        }
    }

    /**
     * 补全智能网格 PostgreSQL 数据源
     */
    private void seedGridDatasources() {
        if (!StringUtils.hasText(gridInitialPassword)) {
            return;
        }
        String encryptedPassword = credentialEncryptUtil.encrypt(gridInitialPassword);
        insertGridDatasource(5L, "中国海洋预报网PG-风", "app_wind_speed_grid", encryptedPassword);
        insertGridDatasource(6L, "中国海洋预报网PG-海浪", "app_wave_height_grid", encryptedPassword);
        insertGridDatasource(7L, "中国海洋预报网PG-海流", "app_current_speed_grid", encryptedPassword);
        insertGridDatasource(8L, "中国海洋预报网PG-海温", "app_sst_grid", encryptedPassword);
        insertGridDatasource(9L, "中国海洋预报网PG-风暴增水", "app_storm_tide_grid", encryptedPassword);
    }

    private void insertGridDatasource(long id, String name, String tableName, String encryptedPassword) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM monitor_datasource WHERE id = ? AND deleted_flag = 0", Integer.class, id);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO monitor_datasource (id, ds_name, ds_type, host, port, database_name, username, password, table_name, status, deleted_flag) "
                        + "VALUES (?, ?, 'postgresql', ?, ?, ?, ?, ?, ?, 1, 0)",
                id, name, "116.204.52.24", 3141, "hyyj", "ocean_work", encryptedPassword, tableName);
        log.info("已补全数据源: {} ({})", name, tableName);
    }

    /**
     * 补全台风风暴潮数据源（密码 AES 加密入库）
     */
    private void seedTyphoonSurgeDatasources() {
        fixTyphoonSurgeDatasourceMetadata();
        if (StringUtils.hasText(typhoonSurgeInitialPassword)) {
            String encrypted = credentialEncryptUtil.encrypt(typhoonSurgeInitialPassword);
            upsertTyphoonDatasource(
                    10L, "台风风暴潮-网站库", "mysql",
                    "116.204.52.200", 3140, "center_site", "ocean_work",
                    encrypted, "data_typhoon_surge_info");
            upsertTyphoonDatasource(
                    11L, "台风风暴潮-PG库", "postgresql",
                    "128.5.2.164", 5432, "web_surge", "ocean_work",
                    encrypted, "tb_typhoon_surge_info");
        }
        if (StringUtils.hasText(typhoonSurgeFtpPassword)) {
            String encrypted = credentialEncryptUtil.encrypt(typhoonSurgeFtpPassword);
            upsertTyphoonDatasource(
                    12L, "台风风暴潮-FTP", "ftp",
                    "128.5.2.164", 21, "", "surge_duty_watcher",
                    encrypted, "/ty_surge/nc_maxsurge");
        }
        if (StringUtils.hasText(typhoonSurgeSharePassword)) {
            String encrypted = credentialEncryptUtil.encrypt(typhoonSurgeSharePassword);
            upsertTyphoonDatasource(
                    13L, "台风风暴潮-原始文件共享", "smb",
                    "172.16.30.160", 445, "upload2surge", "upload2surge",
                    encrypted, "ty_surge/result");
        }
    }

    private void fixTyphoonSurgeDatasourceMetadata() {
        jdbcTemplate.update(
                "UPDATE monitor_datasource SET database_name = ? WHERE id = 10 AND deleted_flag = 0 AND database_name = ?",
                "center_site", "hyyj");
        jdbcTemplate.update(
                "UPDATE monitor_datasource SET database_name = ? WHERE id = 11 AND deleted_flag = 0 AND database_name = ?",
                "web_surge", "hyyj");
        jdbcTemplate.update(
                "UPDATE monitor_datasource SET table_name = ? WHERE id = 13 AND deleted_flag = 0 AND table_name = ?",
                "ty_surge/result", "ty_surge");
    }

    private void upsertTyphoonDatasource(long id, String name, String dsType, String host, int port,
                                         String databaseName, String username, String encryptedPassword,
                                         String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM monitor_datasource WHERE id = ? AND deleted_flag = 0", Integer.class, id);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO monitor_datasource (id, ds_name, ds_type, host, port, database_name, username, password, table_name, status, deleted_flag) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0)",
                    id, name, dsType, host, port, databaseName, username, encryptedPassword, tableName);
            log.info("已补全数据源: {} ({})", name, tableName);
            return;
        }
        String currentPassword = jdbcTemplate.queryForObject(
                "SELECT password FROM monitor_datasource WHERE id = ? AND deleted_flag = 0",
                String.class, id);
        if (!credentialEncryptUtil.isEncrypted(currentPassword)) {
            jdbcTemplate.update(
                    "UPDATE monitor_datasource SET password = ? WHERE id = ? AND deleted_flag = 0",
                    encryptedPassword, id);
            log.info("已加密更新 monitor_datasource.{} 密码", id);
        }
    }

    private void seedTyphoonSurgeModule() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM monitor_module WHERE id = 36 AND deleted_flag = 0", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        int inserted = jdbcTemplate.update(
                "INSERT INTO monitor_module (id, site_id, module_name, module_url, module_category, module_group, "
                        + "check_type, check_param, expected_time, status, deleted_flag) "
                        + "VALUES (36, 1, '台风风暴潮', 'https://www.oceanguide.org.cn/IndexHome', "
                        + "'forecast_service', '中心网站-风暴潮预报', 'TYPHOON_STORM_SURGE_CHAIN', "
                        + "'{\"mysqlDatasourceId\":\"10\",\"pgDatasourceId\":\"11\",\"ftpDatasourceId\":\"12\",\"shareDatasourceId\":\"13\"}', "
                        + "'08:00', 1, 0)");
        if (inserted > 0) {
            log.info("已补全监控模块: 台风风暴潮");
        }
    }

    /**
     * 环境预报模块改为引用独立数据源，不再在检测参数中写表名
     */
    private void migrateEnvModuleCheckParams() {
        updateModuleCheckParam(28L,
                "{\"datasourceId\":\"2\",\"timeField\":\"create_date\",\"titleField\":\"name\",\"scheduleType\":\"daily\"}");
        updateModuleCheckParam(29L,
                "{\"datasourceId\":\"3\",\"timeField\":\"create_date\",\"titleField\":\"code\",\"scheduleType\":\"daily\"}");
        updateModuleCheckParam(30L,
                "{\"datasourceId\":\"4\",\"timeField\":\"create_date\",\"titleField\":\"title\",\"scheduleType\":\"monthly\",\"categoryId\":\"1190087852779372544\"}");
    }

    private void updateModuleCheckParam(Long moduleId, String newParam) {
        String current = jdbcTemplate.queryForObject(
                "SELECT check_param FROM monitor_module WHERE id = ?", String.class, moduleId);
        if (newParam.equals(current)) {
            return;
        }
        int updated = jdbcTemplate.update(
                "UPDATE monitor_module SET check_param = ? WHERE id = ?", newParam, moduleId);
        if (updated > 0) {
            log.info("已更新 monitor_module.{} 检测参数，关联数据源管理", moduleId);
        }
    }

    /**
     * 将数据源明文密码迁移为 AES 密文
     */
    private void migrateDatasourcePasswords() {
        List<Map<String, Object>> datasources = jdbcTemplate.queryForList(
                "SELECT id, password FROM monitor_datasource WHERE deleted_flag = 0");
        for (Map<String, Object> datasource : datasources) {
            String password = String.valueOf(datasource.get("password"));
            if (credentialEncryptUtil.isEncrypted(password)) {
                continue;
            }
            String encoded = credentialEncryptUtil.encrypt(password);
            jdbcTemplate.update("UPDATE monitor_datasource SET password = ? WHERE id = ?",
                    encoded, datasource.get("id"));
            log.info("已加密 monitor_datasource.{} 密码", datasource.get("id"));
        }
    }

    /**
     * 初始化默认用户
     */
    private void seedDefaultUsers() {
        Integer dutyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = 'duty'", Integer.class);
        if (dutyCount != null && dutyCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_user (username, password, real_name, role, status) VALUES (?, ?, ?, ?, ?)",
                    "duty", "duty123", "值班人员", "duty", 1);
            log.info("已初始化默认值班账号 duty / duty123");
        }
    }

    /**
     * 将明文密码迁移为 BCrypt
     */
    private void migratePlainPasswords() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, password FROM sys_user WHERE deleted_flag = 0");
        for (Map<String, Object> user : users) {
            String password = String.valueOf(user.get("password"));
            if (password.startsWith("$2a$") || password.startsWith("$2b$")) {
                continue;
            }
            String encoded = passwordEncoder.encode(password);
            jdbcTemplate.update("UPDATE sys_user SET password = ? WHERE id = ?", encoded, user.get("id"));
            log.info("已加密 sys_user.{} 密码", user.get("id"));
        }
    }

    /**
     * 同步默认监控网站地址（仅替换旧版默认地址）
     */
    private void syncDefaultSiteUrls() {
        updateSiteUrlIfLegacy(1L, "https://www.oceanguide.org.cn/IndexHome",
                "https://www.oceanguide.org.cn/", "http://www.oceanguide.org.cn/");
        updateSiteUrlIfLegacy(4L, "https://neargoos.nmefc.cn/#/index",
                "https://www.neargoos.org/", "http://www.neargoos.org/");
        updateSiteUrlIfLegacy(5L, "https://macom.oceanguide.org.cn/",
                "https://www.macom.cn/", "http://www.macom.cn/");
    }

    private void updateSiteUrlIfLegacy(Long id, String newUrl, String... legacyUrls) {
        for (String legacyUrl : legacyUrls) {
            int updated = jdbcTemplate.update(
                    "UPDATE monitor_site SET site_url = ? WHERE id = ? AND site_url = ?",
                    newUrl, id, legacyUrl);
            if (updated > 0) {
                log.info("已更新 monitor_site.{} 地址: {} -> {}", id, legacyUrl, newUrl);
                return;
            }
        }
    }

    private Set<String> loadTableColumns(String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("PRAGMA table_info(" + tableName + ")");
        Set<String> columns = new HashSet<>();
        for (Map<String, Object> row : rows) {
            columns.add(String.valueOf(row.get("name")));
        }
        return columns;
    }

    private void addColumnIfMissing(String tableName, Set<String> columns, String columnName, String definition) {
        if (columns.contains(columnName)) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        log.info("已添加 {}.{}", tableName, columnName);
    }
}
