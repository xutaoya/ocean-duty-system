package com.oceanduty.third.mysql;

import com.oceanduty.module.monitor.domain.CmsForecastAlarmRecord;
import com.oceanduty.module.monitor.domain.CmsTablePublishRecord;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.util.CredentialEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * CMS 灾害预警表查询客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CmsForecastAlarmQueryClient {

    private static final int CONNECT_TIMEOUT_SECONDS = 10;

    private static final String DETAIL_COLUMNS = """
            title, code, type, alarm_date, level, image, description, defense_guide, standard, content
            """;

    private final CredentialEncryptUtil credentialEncryptUtil;

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
        String url = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=%d&serverTimezone=Asia/Shanghai",
                datasource.getHost(),
                datasource.getPort() == null ? 3306 : datasource.getPort(),
                datasource.getDatabaseName(),
                CONNECT_TIMEOUT_SECONDS * 1000);
        return DriverManager.getConnection(url, datasource.getUsername(),
                credentialEncryptUtil.decrypt(datasource.getPassword()));
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
