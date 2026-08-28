package com.oceanduty.third.mysql;

import com.oceanduty.module.monitor.domain.CmsForecastAlarmRecord;
import com.oceanduty.module.monitor.domain.CmsTablePublishRecord;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.util.CredentialEncryptUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CMS 灾害预警表查询客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CmsForecastAlarmQueryClient {

    private static final int CONNECT_TIMEOUT_SECONDS = 5;
    private static final int POOL_SIZE = 4;

    private static final String DETAIL_COLUMNS = """
            title, code, type, alarm_date, level, image, description, defense_guide, standard, content
            """;

    private final CredentialEncryptUtil credentialEncryptUtil;
    private final ConcurrentHashMap<Long, HikariDataSource> dataSourcePools = new ConcurrentHashMap<>();

    /**
     * 查询指定类型的最新警报摘要
     */
    public CmsForecastAlarmRecord fetchLatest(MonitorDatasourceEntity datasource, String alarmType) {
        CmsForecastAlarmRecord record = fetchLatestDetail(datasource, alarmType);
        if (record == null) {
            return null;
        }
        return CmsForecastAlarmRecord.builder()
                .type(record.getType())
                .title(record.getTitle())
                .code(record.getCode())
                .alarmDate(record.getAlarmDate())
                .build();
    }

    /**
     * 查询指定类型的最新警报完整信息
     */
    public CmsForecastAlarmRecord fetchLatestDetail(MonitorDatasourceEntity datasource, String alarmType) {
        if (datasource == null || !StringUtils.hasText(alarmType)) {
            return null;
        }
        validateIdentifier(datasource.getTableName());

        String sql = "SELECT " + DETAIL_COLUMNS + " FROM `" + datasource.getTableName()
                + "` WHERE type = ? AND del_flag = '0' ORDER BY alarm_date DESC LIMIT 1";

        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, alarmType);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRecord(resultSet);
            }
        } catch (SQLException e) {
            log.error("查询 CMS 灾害预警详情失败: dsId={}, type={}, msg={}",
                    datasource.getId(), alarmType, e.getMessage());
            return null;
        }
    }

    /**
     * 查询表最新更新时间（MySQL / PostgreSQL）
     */
    public CmsTablePublishRecord fetchLatestUpdate(MonitorDatasourceEntity datasource, String table, String timeField) {
        if (datasource == null || !StringUtils.hasText(table) || !StringUtils.hasText(timeField)) {
            return null;
        }
        validateIdentifier(table);
        validateIdentifier(timeField);

        String sql = buildLatestUpdateSql(datasource.getDsType(), table, timeField);
        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }
            Timestamp publishTime = resultSet.getTimestamp("publish_time");
            return CmsTablePublishRecord.builder()
                    .publishTime(publishTime == null ? null : publishTime.toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            log.error("查询表最新更新时间失败: dsId={}, table={}, msg={}",
                    datasource.getId(), table, e.getMessage());
            return null;
        }
    }

    /**
     * 查询 CMS 表当日/当月最新发布记录
     */
    public CmsTablePublishRecord fetchTablePublish(MonitorDatasourceEntity datasource, String table,
                                                   String timeField, String titleField, String scheduleType,
                                                   String categoryId) {
        if (datasource == null || !StringUtils.hasText(table) || !StringUtils.hasText(timeField)) {
            return null;
        }
        validateIdentifier(table);
        validateIdentifier(timeField);
        if (StringUtils.hasText(titleField)) {
            validateIdentifier(titleField);
        }

        String titleColumn = StringUtils.hasText(titleField) ? "`" + titleField + "`" : "''";
        // 月更模块也取最近一次发布（月报常在月末发布，标题归属当月）
        String sql = "SELECT " + titleColumn + " AS title, `" + timeField + "` AS publish_time FROM `" + table + "`"
                + " WHERE 1=1"
                + (StringUtils.hasText(categoryId) ? " AND category_id = ?" : "")
                + " ORDER BY `" + timeField + "` DESC LIMIT 1";

        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (StringUtils.hasText(categoryId)) {
                statement.setString(1, categoryId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Timestamp publishTime = resultSet.getTimestamp("publish_time");
                return CmsTablePublishRecord.builder()
                        .title(trimText(resultSet.getString("title")))
                        .publishTime(publishTime == null ? null : publishTime.toLocalDateTime())
                        .build();
            }
        } catch (SQLException e) {
            log.error("查询 CMS 表发布记录失败: dsId={}, table={}, msg={}",
                    datasource.getId(), table, e.getMessage());
            return null;
        }
    }

    /**
     * 查询 PG 表最新 version 字段
     */
    public String fetchLatestVersion(MonitorDatasourceEntity datasource, String table, String versionField) {
        if (datasource == null || !StringUtils.hasText(table) || !StringUtils.hasText(versionField)) {
            return null;
        }
        validateIdentifier(table);
        validateIdentifier(versionField);

        String timeField = "update_date";
        validateIdentifier(timeField);
        String sql = "SELECT " + quoteIdentifier(datasource.getDsType(), versionField) + " AS version_value"
                + " FROM " + quoteIdentifier(datasource.getDsType(), table)
                + " ORDER BY " + quoteIdentifier(datasource.getDsType(), timeField) + " DESC LIMIT 1";

        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }
            return trimText(resultSet.getString("version_value"));
        } catch (SQLException e) {
            log.error("查询 PG version 失败: dsId={}, table={}, msg={}",
                    datasource.getId(), table, e.getMessage());
            return null;
        }
    }

    /**
     * 根据 local_version 查询智能网格起报时间
     */
    public LocalDateTime fetchReportDate(MonitorDatasourceEntity datasource, String controllerTable, String localVersion) {
        if (datasource == null || !StringUtils.hasText(controllerTable) || !StringUtils.hasText(localVersion)) {
            return null;
        }
        validateIdentifier(controllerTable);

        String sql = "SELECT report_date FROM `" + controllerTable + "` WHERE local_version = ? LIMIT 1";
        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, localVersion);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Timestamp reportDate = resultSet.getTimestamp("report_date");
                return reportDate == null ? null : reportDate.toLocalDateTime();
            }
        } catch (SQLException e) {
            log.error("查询起报时间失败: dsId={}, version={}, msg={}",
                    datasource.getId(), localVersion, e.getMessage());
            return null;
        }
    }

    /**
     * 测试数据源连接
     */
    public boolean testConnection(MonitorDatasourceEntity datasource) {
        try (Connection connection = openConnection(datasource)) {
            return connection.isValid(CONNECT_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            log.warn("数据源连接测试失败: dsId={}, msg={}", datasource.getId(), e.getMessage());
            return false;
        }
    }

    private CmsForecastAlarmRecord mapRecord(ResultSet resultSet) throws SQLException {
        Timestamp alarmDate = resultSet.getTimestamp("alarm_date");
        return CmsForecastAlarmRecord.builder()
                .type(trimText(resultSet.getString("type")))
                .title(trimText(resultSet.getString("title")))
                .code(trimText(resultSet.getString("code")))
                .alarmDate(alarmDate == null ? null : alarmDate.toLocalDateTime())
                .level(trimText(resultSet.getString("level")))
                .image(trimText(resultSet.getString("image")))
                .description(trimText(resultSet.getString("description")))
                .defenseGuide(trimText(resultSet.getString("defense_guide")))
                .standard(trimText(resultSet.getString("standard")))
                .content(trimText(resultSet.getString("content")))
                .build();
    }

    private Connection openConnection(MonitorDatasourceEntity datasource) throws SQLException {
        HikariDataSource pool = dataSourcePools.computeIfAbsent(datasource.getId(), id -> createPool(datasource));
        return pool.getConnection();
    }

    @PreDestroy
    public void shutdownPools() {
        dataSourcePools.values().forEach(HikariDataSource::close);
        dataSourcePools.clear();
    }

    private HikariDataSource createPool(MonitorDatasourceEntity datasource) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("cms-ds-" + datasource.getId());
        config.setJdbcUrl(buildJdbcUrl(datasource));
        config.setUsername(datasource.getUsername());
        config.setPassword(credentialEncryptUtil.decrypt(datasource.getPassword()));
        config.setMaximumPoolSize(POOL_SIZE);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(CONNECT_TIMEOUT_SECONDS * 1000L);
        config.setIdleTimeout(60_000L);
        config.setMaxLifetime(300_000L);
        return new HikariDataSource(config);
    }

    private String buildJdbcUrl(MonitorDatasourceEntity datasource) {
        if (isPostgresql(datasource.getDsType())) {
            return String.format(
                    "jdbc:postgresql://%s:%d/%s?connectTimeout=%d",
                    datasource.getHost(),
                    datasource.getPort() == null ? 5432 : datasource.getPort(),
                    datasource.getDatabaseName(),
                    CONNECT_TIMEOUT_SECONDS);
        }
        return String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=%d&serverTimezone=Asia/Shanghai",
                datasource.getHost(),
                datasource.getPort() == null ? 3306 : datasource.getPort(),
                datasource.getDatabaseName(),
                CONNECT_TIMEOUT_SECONDS * 1000);
    }

    private boolean isPostgresql(String dsType) {
        return "postgresql".equalsIgnoreCase(dsType) || "postgres".equalsIgnoreCase(dsType);
    }

    private String quoteIdentifier(String dsType, String identifier) {
        if (isPostgresql(dsType)) {
            return identifier;
        }
        return "`" + identifier + "`";
    }

    private String buildLatestUpdateSql(String dsType, String table, String timeField) {
        String timeColumn = quoteIdentifier(dsType, timeField);
        String tableName = quoteIdentifier(dsType, table);
        return "SELECT " + timeColumn + " AS publish_time FROM " + tableName
                + " ORDER BY " + timeColumn + " DESC LIMIT 1";
    }

    private void validateIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier) || !identifier.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("非法表名: " + identifier);
        }
    }

    private String trimText(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.replace("\r", "").replace("\n", "").trim();
    }
}
