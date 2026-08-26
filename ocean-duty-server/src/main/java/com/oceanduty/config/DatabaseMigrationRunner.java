package com.oceanduty.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Override
    public void run(String... args) {
        Set<String> moduleColumns = loadTableColumns("monitor_module");
        addColumnIfMissing("monitor_module", moduleColumns, "module_category", "VARCHAR(50) NOT NULL DEFAULT 'disaster_warning'");
        addColumnIfMissing("monitor_module", moduleColumns, "module_group", "VARCHAR(50)");
        addColumnIfMissing("monitor_module", moduleColumns, "check_type", "VARCHAR(50) NOT NULL DEFAULT 'WARN_HISTORY'");
        addColumnIfMissing("monitor_module", moduleColumns, "check_param", "VARCHAR(500)");

        Set<String> siteColumns = loadTableColumns("monitor_site");
        addColumnIfMissing("monitor_site", siteColumns, "timeout_ms", "INTEGER NOT NULL DEFAULT 10000");
        addColumnIfMissing("monitor_site", siteColumns, "response_threshold", "INTEGER NOT NULL DEFAULT 3000");
        syncDefaultSiteUrls();
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
