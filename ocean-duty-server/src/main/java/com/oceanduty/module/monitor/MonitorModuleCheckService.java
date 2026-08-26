package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.CmsForecastAlarmRecord;
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

/**
 * 模块更新时间检测服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorModuleCheckService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
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
     * 检测 CMS 灾害预警模块
     */
    public void checkCmsForecastAlarmModules() {
        List<MonitorModuleEntity> modules = monitorModuleDao.selectList(null);
        for (MonitorModuleEntity module : modules) {
            if (ModuleCheckTypeConst.CMS_FORECAST_ALARM.equals(module.getCheckType())) {
                checkModule(module);
            }
        }
    }

    /**
     * 检测单个模块
     */
    public void checkModule(MonitorModuleEntity module) {
        Map<String, String> params = parseCheckParam(module.getCheckParam());
        ModuleCheckResult result = fetchCheckResult(module.getCheckType(), params);
        module.setDataUpdateTime(result.updateTime());
        module.setAlarmTitle(result.alarmTitle());
        module.setAlarmCode(result.alarmCode());
        module.setAlarmLevel(result.alarmLevel());
        module.setLastCheckTime(LocalDateTime.now());
        module.setStatus(evaluateStatus(module, result.updateTime()));
        if (result.updateTime() == null) {
            module.setRemark("未获取到更新时间");
        } else {
            module.setRemark(null);
        }
        monitorModuleDao.updateById(module);
        log.info("模块检测完成: {} -> status={}, updateTime={}, title={}, code={}",
                module.getModuleName(), module.getStatus(), result.updateTime(),
                result.alarmTitle(), result.alarmCode());
    }

    private boolean shouldCheckModule(MonitorModuleEntity module) {
        if (ModuleCheckTypeConst.CMS_FORECAST_ALARM.equals(module.getCheckType())) {
            return true;
        }
        return moduleCheckEnabled;
    }

    private ModuleCheckResult fetchCheckResult(String checkType, Map<String, String> params) {
        if (!StringUtils.hasText(checkType)) {
            return ModuleCheckResult.empty();
        }
        if (ModuleCheckTypeConst.CMS_FORECAST_ALARM.equals(checkType)) {
            return fetchCmsForecastAlarm(params);
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
    private Integer evaluateStatus(MonitorModuleEntity module, LocalDateTime updateTime) {
        if (updateTime == null) {
            return MonitorStatusConst.ERROR;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!StringUtils.hasText(module.getExpectedTime())) {
            return updateTime.isAfter(now.minusHours(24)) ? MonitorStatusConst.NORMAL : MonitorStatusConst.WARNING;
        }

        LocalTime expected = LocalTime.parse(module.getExpectedTime(), TIME_FORMATTER);
        LocalDateTime expectedToday = LocalDateTime.of(LocalDate.now(), expected);
        if (updateTime.isAfter(expectedToday) || updateTime.isEqual(expectedToday)) {
            return MonitorStatusConst.NORMAL;
        }
        if (now.isAfter(expectedToday.plusHours(2))) {
            return MonitorStatusConst.ERROR;
        }
        if (now.isAfter(expectedToday)) {
            return MonitorStatusConst.WARNING;
        }
        return updateTime.toLocalDate().isBefore(LocalDate.now()) ? MonitorStatusConst.WARNING : MonitorStatusConst.NORMAL;
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

    private record ModuleCheckResult(LocalDateTime updateTime, String alarmTitle, String alarmCode, String alarmLevel) {
        private static ModuleCheckResult empty() {
            return new ModuleCheckResult(null, null, null, null);
        }
    }
}
