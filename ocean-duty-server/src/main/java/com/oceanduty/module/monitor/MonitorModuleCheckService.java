package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.third.nmefc.NmefcApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final NmefcApiClient nmefcApiClient;

    /**
     * 检测全部模块更新时间
     */
    public void checkAllModules() {
        List<MonitorModuleEntity> modules = monitorModuleDao.selectList(null);
        for (MonitorModuleEntity module : modules) {
            checkModule(module);
        }
    }

    /**
     * 检测单个模块
     */
    public void checkModule(MonitorModuleEntity module) {
        Map<String, String> params = parseCheckParam(module.getCheckParam());
        LocalDateTime updateTime = fetchUpdateTime(module.getCheckType(), params);
        module.setDataUpdateTime(updateTime);
        module.setLastCheckTime(LocalDateTime.now());
        module.setStatus(evaluateStatus(module, updateTime));
        if (updateTime == null) {
            module.setRemark("未获取到更新时间");
        } else {
            module.setRemark(null);
        }
        monitorModuleDao.updateById(module);
        log.info("模块检测完成: {} -> status={}, updateTime={}", module.getModuleName(), module.getStatus(), updateTime);
    }

    private LocalDateTime fetchUpdateTime(String checkType, Map<String, String> params) {
        if (!StringUtils.hasText(checkType)) {
            return null;
        }
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
}
