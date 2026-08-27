package com.oceanduty.config;

import com.oceanduty.util.CredentialEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        migrateEnvModuleCheckParams();
        removeLegacyNmefcModules();
        fixTodayPublishedModuleStatus();
        fixMonthlyPublishedModuleStatus();
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
     * 环境预报模块改为引用独立数据源，不再在检测参数中写表名
     */
    private void migrateEnvModuleCheckParams() {
        updateModuleCheckParam(28L,
                "{\"datasourceId\":\"2\",\"timeField\":\"create_date\",\"titleField\":\"name\",\"scheduleType\":\"daily\"}");
        updateModuleCheckParam(29L,
                "{\"datasourceId\":\"3\",\"timeField\":\"create_date\",\"titleField\":\"name\",\"scheduleType\":\"daily\"}");
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
