package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.CmsForecastAlarmRecord;
import com.oceanduty.module.monitor.domain.CmsTablePublishRecord;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.third.mysql.CmsForecastAlarmQueryClient;
import com.oceanduty.third.nmefc.NmefcApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模块更新时间检测服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorModuleCheckService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern TITLE_MONTH_PATTERN = Pattern.compile("(\\d{4})年0?(\\d{1,2})月");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MonitorModuleDao monitorModuleDao;
    private final MonitorDatasourceDao monitorDatasourceDao;
    private final NmefcApiClient nmefcApiClient;
    private final CmsForecastAlarmQueryClient cmsForecastAlarmQueryClient;

    @Value("${ocean-duty.monitor.module-check-enabled:false}")
    private boolean moduleCheckEnabled;

    /**
     * 检测全部模块更新时间
     */
    public void checkAllModules() {
        List<MonitorModuleEntity> modules = monitorModuleDao.selectList(null);
        for (MonitorModuleEntity module : modules) {
            if (shouldCheckModule(module)) {
                checkModule(module);
            }
        }
    }

    /**
     * 检测 CMS 模块（灾害预警 + 环境预报等）
     */
    public void checkCmsModules() {
        List<MonitorModuleEntity> modules = monitorModuleDao.selectList(null);
        for (MonitorModuleEntity module : modules) {
            if (isCmsCheckType(module.getCheckType())) {
                checkModule(module);
            }
        }
    }

    /**
     * 检测 CMS 灾害预警模块（兼容旧调用）
     */
    public void checkCmsForecastAlarmModules() {
        checkCmsModules();
    }

    /**
     * 检测单个模块
     */
    public void checkModule(MonitorModuleEntity module) {
        Map<String, String> params = parseCheckParam(module.getCheckParam());
        String datasourceIssue = resolveDatasourceIssue(module.getCheckType(), params);
        if (datasourceIssue != null) {
            module.setDataUpdateTime(null);
            module.setAlarmTitle(null);
            module.setAlarmCode(null);
            module.setAlarmLevel(null);
            module.setLastCheckTime(LocalDateTime.now());
            module.setStatus(MonitorStatusConst.ERROR);
            module.setRemark(datasourceIssue);
            monitorModuleDao.updateById(module);
            log.info("模块检测跳过: {} -> remark={}", module.getModuleName(), datasourceIssue);
            return;
        }

        ModuleCheckResult result = fetchCheckResult(module.getCheckType(), params);
        module.setDataUpdateTime(result.updateTime());
        module.setAlarmTitle(result.alarmTitle());
        module.setAlarmCode(result.alarmCode());
        module.setAlarmLevel(result.alarmLevel());
        module.setLastCheckTime(LocalDateTime.now());
        module.setStatus(evaluateStatus(module, result.updateTime(), result.alarmTitle(), params));
        if (result.updateTime() == null) {
            module.setRemark(resolveEmptyRemark(params));
        } else {
            module.setRemark(null);
        }
        monitorModuleDao.updateById(module);
        log.info("模块检测完成: {} -> status={}, updateTime={}, title={}, code={}",
                module.getModuleName(), module.getStatus(), result.updateTime(),
                result.alarmTitle(), result.alarmCode());
    }

    private boolean shouldCheckModule(MonitorModuleEntity module) {
        if (isCmsCheckType(module.getCheckType())) {
            return true;
        }
        return moduleCheckEnabled;
    }

    private boolean isCmsCheckType(String checkType) {
        return ModuleCheckTypeConst.CMS_FORECAST_ALARM.equals(checkType)
                || ModuleCheckTypeConst.CMS_TABLE_PUBLISH.equals(checkType)
                || ModuleCheckTypeConst.CMS_GRID_UPDATE.equals(checkType);
    }

    private ModuleCheckResult fetchCheckResult(String checkType, Map<String, String> params) {
        if (!StringUtils.hasText(checkType)) {
            return ModuleCheckResult.empty();
        }
        if (ModuleCheckTypeConst.CMS_FORECAST_ALARM.equals(checkType)) {
            return fetchCmsForecastAlarm(params);
        }
        if (ModuleCheckTypeConst.CMS_TABLE_PUBLISH.equals(checkType)) {
            return fetchCmsTablePublish(params);
        }
        if (ModuleCheckTypeConst.CMS_GRID_UPDATE.equals(checkType)) {
            return fetchCmsGridUpdate(params);
        }
        LocalDateTime updateTime = fetchUpdateTime(checkType, params);
        return new ModuleCheckResult(updateTime, null, null, null);
    }

    private ModuleCheckResult fetchCmsForecastAlarm(Map<String, String> params) {
        Long datasourceId = parseLong(params.get("datasourceId"));
        String alarmType = params.get("type");
        if (datasourceId == null || !StringUtils.hasText(alarmType)) {
            return ModuleCheckResult.empty();
        }
        MonitorDatasourceEntity datasource = monitorDatasourceDao.selectById(datasourceId);
        if (datasource == null || datasource.getStatus() != null && datasource.getStatus() == 0) {
            return ModuleCheckResult.empty();
        }
        CmsForecastAlarmRecord record = cmsForecastAlarmQueryClient.fetchLatest(datasource, alarmType);
        if (record == null) {
            return ModuleCheckResult.empty();
        }
        return new ModuleCheckResult(record.getAlarmDate(), record.getTitle(), record.getCode(), record.getLevel());
    }

    private ModuleCheckResult fetchCmsTablePublish(Map<String, String> params) {
        Long datasourceId = parseLong(params.get("datasourceId"));
        String timeField = params.get("timeField");
        String titleField = params.get("titleField");
        String scheduleType = params.getOrDefault("scheduleType", "daily");
        String categoryId = params.get("categoryId");
        if (datasourceId == null || !StringUtils.hasText(timeField)) {
            return ModuleCheckResult.empty();
        }
        MonitorDatasourceEntity datasource = monitorDatasourceDao.selectById(datasourceId);
        if (datasource == null || datasource.getStatus() != null && datasource.getStatus() == 0) {
            return ModuleCheckResult.empty();
        }
        String table = resolveTableName(params, datasource);
        if (!StringUtils.hasText(table)) {
            return ModuleCheckResult.empty();
        }
        CmsTablePublishRecord record = cmsForecastAlarmQueryClient.fetchTablePublish(
                datasource, table, timeField, titleField, scheduleType, categoryId);
        if (record == null) {
            return ModuleCheckResult.empty();
        }
        return new ModuleCheckResult(record.getPublishTime(), record.getTitle(), null, null);
    }

    private ModuleCheckResult fetchCmsGridUpdate(Map<String, String> params) {
        Long datasourceId = parseLong(params.get("datasourceId"));
        String timeField = params.getOrDefault("timeField", "update_date");
        if (datasourceId == null) {
            return ModuleCheckResult.empty();
        }
        MonitorDatasourceEntity datasource = monitorDatasourceDao.selectById(datasourceId);
        if (datasource == null || datasource.getStatus() != null && datasource.getStatus() == 0) {
            return ModuleCheckResult.empty();
        }
        String table = resolveTableName(params, datasource);
        if (!StringUtils.hasText(table)) {
            return ModuleCheckResult.empty();
        }
        CmsTablePublishRecord record = cmsForecastAlarmQueryClient.fetchLatestUpdate(datasource, table, timeField);
        if (record == null || record.getPublishTime() == null) {
            return ModuleCheckResult.empty();
        }
        return new ModuleCheckResult(record.getPublishTime(), null, null, null);
    }

    private LocalDateTime fetchUpdateTime(String checkType, Map<String, String> params) {
        return switch (checkType) {
            case ModuleCheckTypeConst.WARN_HISTORY -> nmefcApiClient.fetchWarnHistoryLatest(
                    params.get("warnType"), params.get("filter"));
            case ModuleCheckTypeConst.ANALYSIS_LIST -> nmefcApiClient.fetchAnalysisLatest(params.get("type"));
            case ModuleCheckTypeConst.NUMERICAL_LIST -> nmefcApiClient.fetchNumericalLatest(
                    params.get("element"), params.get("regioncode"));
            case ModuleCheckTypeConst.DATA_INIT -> nmefcApiClient.fetchInitLatest(params.get("key"));
            case ModuleCheckTypeConst.DEEPSEA_INFO -> nmefcApiClient.fetchDeepseaLatest(
                    params.get("region"), params.get("element"));
            case ModuleCheckTypeConst.POLAR_REGIONS_LIST -> nmefcApiClient.fetchPolarLatest(params.get("region"));
            default -> null;
        };
    }

    /**
     * 根据预期更新时间和实际更新时间判断状态
     */
    private Integer evaluateStatus(MonitorModuleEntity module, LocalDateTime updateTime, String alarmTitle,
                                   Map<String, String> params) {
        if (ModuleCheckTypeConst.CMS_GRID_UPDATE.equals(module.getCheckType())) {
            return evaluateGridWindowStatus(updateTime, params.get("windowPreset"));
        }
        if (ModuleCheckTypeConst.CMS_TABLE_PUBLISH.equals(module.getCheckType())) {
            String scheduleType = params.getOrDefault("scheduleType", "daily");
            if ("monthly".equals(scheduleType)) {
                return evaluateMonthlyStatus(updateTime, alarmTitle);
            }
            return evaluateDailyStatus(module, updateTime);
        }
        return evaluateAlarmStatus(module, updateTime);
    }

    private Integer evaluateAlarmStatus(MonitorModuleEntity module, LocalDateTime updateTime) {
        if (updateTime == null) {
            return MonitorStatusConst.ERROR;
        }

        LocalDate today = LocalDate.now();
        // 当天任意时间发布均视为正常
        if (updateTime.toLocalDate().equals(today)) {
            return MonitorStatusConst.NORMAL;
        }

        if (!StringUtils.hasText(module.getExpectedTime())) {
            return updateTime.isAfter(LocalDateTime.now().minusHours(24))
                    ? MonitorStatusConst.NORMAL
                    : MonitorStatusConst.ERROR;
        }

        LocalTime expected = LocalTime.parse(module.getExpectedTime(), TIME_FORMATTER);
        LocalDateTime deadline = LocalDateTime.of(today, expected);
        if (LocalDateTime.now().isBefore(deadline)) {
            return MonitorStatusConst.NORMAL;
        }

        return MonitorStatusConst.ERROR;
    }

    private Integer evaluateDailyStatus(MonitorModuleEntity module, LocalDateTime updateTime) {
        if (updateTime == null) {
            return MonitorStatusConst.ERROR;
        }

        LocalDate today = LocalDate.now();
        // 当天任意时间发布均视为正常，不要求达到预期发布时间
        if (updateTime.toLocalDate().equals(today)) {
            return MonitorStatusConst.NORMAL;
        }

        if (!StringUtils.hasText(module.getExpectedTime())) {
            return MonitorStatusConst.ERROR;
        }

        LocalTime expected = LocalTime.parse(module.getExpectedTime(), TIME_FORMATTER);
        LocalDateTime deadline = LocalDateTime.of(today, expected);
        if (LocalDateTime.now().isBefore(deadline)) {
            return MonitorStatusConst.NORMAL;
        }

        return MonitorStatusConst.ERROR;
    }

    private Integer evaluateGridWindowStatus(LocalDateTime updateTime, String windowPreset) {
        if (updateTime == null) {
            return MonitorStatusConst.ERROR;
        }
        LocalDateTime now = LocalDateTime.now();
        if (GridWindowEvaluator.isUpdateFresh(updateTime, windowPreset, now)) {
            return MonitorStatusConst.NORMAL;
        }
        return MonitorStatusConst.ERROR;
    }

    private Integer evaluateMonthlyStatus(LocalDateTime updateTime, String alarmTitle) {
        LocalDate today = LocalDate.now();
        if (updateTime != null
                && updateTime.getYear() == today.getYear()
                && updateTime.getMonthValue() == today.getMonthValue()) {
            return MonitorStatusConst.NORMAL;
        }
        if (titleMatchesCurrentMonth(alarmTitle)) {
            return MonitorStatusConst.NORMAL;
        }
        return MonitorStatusConst.ERROR;
    }

    private boolean titleMatchesCurrentMonth(String title) {
        if (!StringUtils.hasText(title)) {
            return false;
        }
        Matcher matcher = TITLE_MONTH_PATTERN.matcher(title);
        if (!matcher.find()) {
            return false;
        }
        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        LocalDate today = LocalDate.now();
        return year == today.getYear() && month == today.getMonthValue();
    }

    private String resolveEmptyRemark(Map<String, String> params) {
        if (StringUtils.hasText(params.get("windowPreset"))) {
            return "未获取到最新更新时间";
        }
        if ("monthly".equals(params.get("scheduleType"))) {
            return "未获取到当月发布记录";
        }
        if ("daily".equals(params.get("scheduleType"))) {
            return "未获取到当日发布记录";
        }
        return "未获取到更新时间";
    }

    /**
     * 检查 CMS 模块关联数据源是否可用
     */
    private String resolveDatasourceIssue(String checkType, Map<String, String> params) {
        if (!isCmsCheckType(checkType)) {
            return null;
        }
        Long datasourceId = parseLong(params.get("datasourceId"));
        if (datasourceId == null) {
            return "未配置关联数据源";
        }
        MonitorDatasourceEntity datasource = monitorDatasourceDao.selectById(datasourceId);
        if (datasource == null) {
            return "关联数据源不存在（ID=" + datasourceId + "）";
        }
        if (datasource.getStatus() != null && datasource.getStatus() == 0) {
            return "关联数据源已禁用：" + datasource.getDsName();
        }
        return null;
    }

    private Map<String, String> parseCheckParam(String checkParam) {
        if (!StringUtils.hasText(checkParam)) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(checkParam, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            log.warn("模块检测参数解析失败: {}", checkParam);
            return Map.of();
        }
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 表名优先取数据源配置，检测参数中的 table 仅作覆盖
     */
    private String resolveTableName(Map<String, String> params, MonitorDatasourceEntity datasource) {
        if (StringUtils.hasText(params.get("table"))) {
            return params.get("table");
        }
        return datasource == null ? null : datasource.getTableName();
    }

    private record ModuleCheckResult(LocalDateTime updateTime, String alarmTitle, String alarmCode, String alarmLevel) {
        private static ModuleCheckResult empty() {
            return new ModuleCheckResult(null, null, null, null);
        }
    }
}
