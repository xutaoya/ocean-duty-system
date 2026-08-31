package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.constant.ModuleCategoryConst;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 与前端展示一致的模块有效状态判定
 */
@Slf4j
public final class MonitorEffectiveStatusUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern MONTH_TITLE_PATTERN = Pattern.compile("(\\d{4})年0?(\\d{1,2})月");

    private MonitorEffectiveStatusUtil() {
    }

    public static Integer resolveEffectiveStatus(MonitorModuleVO module) {
        if (module == null || module.getStatus() == null) {
            return MonitorStatusConst.NORMAL;
        }
        if (ModuleCheckTypeConst.CMS_GRID_UPDATE.equals(module.getCheckType())) {
            return module.getStatus();
        }
        if (isContentCurrent(module)) {
            return MonitorStatusConst.NORMAL;
        }
        return module.getStatus();
    }

    public static boolean isAbnormal(Integer status) {
        return status != null && !MonitorStatusConst.NORMAL.equals(status);
    }

    /**
     * 灾害预警仅展示警报内容，不参与值班日志异常统计
     */
    public static boolean shouldIncludeInDutyLog(MonitorModuleVO module) {
        if (module == null) {
            return false;
        }
        return !ModuleCategoryConst.DISASTER_WARNING.equals(module.getModuleCategory());
    }

    public static boolean isDutyLogAbnormal(MonitorModuleVO module) {
        if (!shouldIncludeInDutyLog(module)) {
            return false;
        }
        return isAbnormal(resolveEffectiveStatus(module));
    }

    private static boolean isContentCurrent(MonitorModuleVO module) {
        Map<String, String> params = parseCheckParam(module.getCheckParam());
        if ("monthly".equals(params.get("scheduleType"))) {
            return isPublishedCurrentMonth(module);
        }
        return isPublishedToday(module.getUpdateTime());
    }

    private static boolean isPublishedCurrentMonth(MonitorModuleVO module) {
        if (titleMatchesCurrentMonth(module.getAlarmTitle())) {
            return true;
        }
        LocalDateTime updateTime = module.getUpdateTime();
        if (updateTime == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return updateTime.getYear() == now.getYear() && updateTime.getMonth() == now.getMonth();
    }

    private static boolean titleMatchesCurrentMonth(String title) {
        if (!StringUtils.hasText(title)) {
            return false;
        }
        Matcher matcher = MONTH_TITLE_PATTERN.matcher(title);
        if (!matcher.find()) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return Integer.parseInt(matcher.group(1)) == now.getYear()
                && Integer.parseInt(matcher.group(2)) == now.getMonthValue();
    }

    private static boolean isPublishedToday(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        return time.toLocalDate().equals(LocalDate.now());
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
