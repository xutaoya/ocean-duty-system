package com.oceanduty.module.monitor;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 智能网格分时段更新阈值判定
 */
public final class GridWindowEvaluator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private GridWindowEvaluator() {
    }

    /**
     * 根据预设与当前时间，解析允许的最大无更新小时数
     */
    public static int resolveMaxHours(String preset, LocalTime now) {
        if (!StringUtils.hasText(preset)) {
            return 24;
        }
        return switch (preset) {
            case "wind" -> 13;
            case "wave" -> inTimeRange(now, "08:00", "22:00") ? 15 : 11;
            case "current", "sst" -> inTimeRange(now, "08:30", "17:30") ? 12 : 16;
            case "storm_tide" -> 24;
            default -> 24;
        };
    }

    /**
     * 判断最新更新时间是否满足当前时段阈值
     */
    public static boolean isUpdateFresh(LocalDateTime updateTime, String preset, LocalDateTime now) {
        if (updateTime == null) {
            return false;
        }
        int maxHours = resolveMaxHours(preset, now.toLocalTime());
        return !now.isAfter(updateTime.plusHours(maxHours));
    }

    /**
     * 判断当前时间是否落在 [start, end) 区间，跨天区间 end 小于 start
     */
    static boolean inTimeRange(LocalTime now, String start, String end) {
        LocalTime startTime = LocalTime.parse(start, TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(end, TIME_FORMATTER);
        if (startTime.isBefore(endTime)) {
            return !now.isBefore(startTime) && now.isBefore(endTime);
        }
        return !now.isBefore(startTime) || now.isBefore(endTime);
    }
}
