package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 依据监控规则推算故障/恢复业务时间
 */
@Slf4j
public final class MonitorEventTimeResolver {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private MonitorEventTimeResolver() {
    }

    public static LocalDateTime resolveModuleFaultTime(MonitorModuleVO module, LocalDateTime now) {
        if (module == null || now == null) {
            return null;
        }
        Map<String, String> params = parseCheckParam(module.getCheckParam());
        LocalDateTime updateTime = module.getUpdateTime();
        if (ModuleCheckTypeConst.CMS_GRID_UPDATE.equals(module.getCheckType())) {
            return resolveGridFaultTime(updateTime, params.get("windowPreset"), now);
        }
        if (ModuleCheckTypeConst.CMS_TABLE_PUBLISH.equals(module.getCheckType())) {
            if ("monthly".equals(params.get("scheduleType"))) {
                return resolveMonthlyFaultTime(module, now);
            }
            return resolveDailyFaultTime(updateTime, module.getExpectedTime(), now);
        }
        return resolveAlarmFaultTime(updateTime, module.getExpectedTime(), now);
    }

    public static LocalDateTime resolveModuleRecoverTime(MonitorModuleVO module) {
        if (module == null) {
            return null;
        }
        return module.getUpdateTime();
    }

    public static LocalDateTime resolveSiteFaultTime(MonitorSiteVO site, LocalDateTime now) {
        if (site == null || !MonitorEffectiveStatusUtil.isAbnormal(site.getStatus())) {
            return null;
        }
        if (site.getLastCheckTime() != null) {
            return site.getLastCheckTime();
        }
        return now;
    }

    public static LocalDateTime resolveSiteRecoverTime(MonitorSiteVO site) {
        if (site == null) {
            return null;
        }
        return site.getLastCheckTime();
    }

    private static LocalDateTime resolveGridFaultTime(LocalDateTime updateTime, String preset, LocalDateTime now) {
        if (updateTime == null) {
            return now;
        }
        int maxHours = GridWindowEvaluator.resolveMaxHours(preset, now.toLocalTime());
        return updateTime.plusHours(maxHours);
    }

    private static LocalDateTime resolveDailyFaultTime(LocalDateTime updateTime, String expectedTime, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (updateTime != null && updateTime.toLocalDate().equals(today)) {
            return updateTime;
        }
        if (StringUtils.hasText(expectedTime)) {
            return LocalDateTime.of(today, LocalTime.parse(expectedTime, TIME_FORMATTER));
        }
        return today.atStartOfDay();
    }

    private static LocalDateTime resolveAlarmFaultTime(LocalDateTime updateTime, String expectedTime, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (updateTime == null) {
            if (StringUtils.hasText(expectedTime)) {
                return LocalDateTime.of(today, LocalTime.parse(expectedTime, TIME_FORMATTER));
            }
            return now.minusHours(24);
        }
        if (updateTime.toLocalDate().equals(today)) {
            return updateTime;
        }
        if (!StringUtils.hasText(expectedTime)) {
            return updateTime.plusHours(24);
        }
        return LocalDateTime.of(today, LocalTime.parse(expectedTime, TIME_FORMATTER));
    }

    private static LocalDateTime resolveMonthlyFaultTime(MonitorModuleVO module, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate faultDate = today.withDayOfMonth(1);
        if (StringUtils.hasText(module.getExpectedTime())) {
            return LocalDateTime.of(faultDate, LocalTime.parse(module.getExpectedTime(), TIME_FORMATTER));
        }
        return faultDate.atStartOfDay();
    }

    private static Map<String, String> parseCheckParam(String checkParam) {
        if (!StringUtils.hasText(checkParam)) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(checkParam, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("模块检测参数解析失败: {}", checkParam);
            return Map.of();
        }
    }
}
