package com.oceanduty.module.duty;

import com.oceanduty.constant.DutyIncidentStatusConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.duty.domain.DutyIncidentEntity;
import com.oceanduty.module.duty.domain.DutyLogChangeEntryVO;
import com.oceanduty.module.duty.domain.DutyLogChangeSummaryVO;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异常-恢复闭环文案
 */
public final class DutyLogClosureFormatter {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private DutyLogClosureFormatter() {
    }

    public static String statusLabel(Integer status) {
        if (MonitorStatusConst.ERROR.equals(status)) {
            return "异常";
        }
        if (MonitorStatusConst.WARNING.equals(status)) {
            return "警告";
        }
        return "正常";
    }

    public static String formatTime(LocalDateTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(TIME_FORMAT);
    }

    public static String formatChangeEntry(DutyLogChangeEntryVO entry) {
        String name = entry.getTargetName();
        return switch (entry.getChangeType()) {
            case "new" -> name + " 新异常(" + entry.getStatusLabel() + ")";
            case "changed" -> name + " 状态变化 "
                    + statusLabel(entry.getPreviousStatus()) + "→" + entry.getStatusLabel();
            case "persistent" -> name + " 持续异常(" + entry.getStatusLabel() + ")";
            case "recovered" -> name + " 已恢复(" + statusLabel(entry.getPreviousStatus()) + "→正常)";
            default -> name;
        };
    }

    public static String buildClosureSummary(DutyLogChangeSummaryVO summary) {
        if (summary == null) {
            return "全部正常，无异常变更";
        }
        List<String> sections = new ArrayList<>();
        appendSection(sections, "恢复", summary.getRecovered());
        appendSection(sections, "新异常", summary.getNewAbnormals());
        appendSection(sections, "状态变化", summary.getChanged());
        appendSection(sections, "持续异常", summary.getPersistent());
        if (sections.isEmpty()) {
            return "全部正常，无异常变更";
        }
        return String.join("；", sections);
    }

    public static String formatIncidentLifecycle(DutyIncidentEntity incident) {
        String prefix = targetLabel(incident.getTargetType(), incident.getTargetName());
        String start = formatTime(incident.getFirstSeenTime()) + " " + statusLabel(incident.getFirstStatus());
        if (DutyIncidentStatusConst.RECOVERED.equals(incident.getIncidentStatus())
                && incident.getRecoveredTime() != null) {
            return prefix + "：" + start + " → " + formatTime(incident.getRecoveredTime()) + " 已恢复";
        }
        return prefix + "：" + start + " → 进行中";
    }

    public static String resolveEventRole(Long logId, DutyIncidentEntity incident) {
        if (logId == null || incident == null) {
            return "related";
        }
        if (logId.equals(incident.getRecoverLogId())) {
            return "recover";
        }
        if (logId.equals(incident.getFirstLogId())) {
            return "start";
        }
        if (logId.equals(incident.getLastLogId())) {
            return "ongoing";
        }
        return "related";
    }

    public static String eventRoleLabel(String eventRole) {
        return switch (eventRole) {
            case "start" -> "异常开始";
            case "recover" -> "已恢复";
            case "ongoing" -> "持续异常";
            default -> "关联事件";
        };
    }

    public static String formatItemTimeline(DutyLogItemEntity item) {
        String prefix = targetLabel(item.getTargetType(), item.getTargetName());
        String changeType = item.getChangeType();
        if (changeType == null) {
            return prefix + " " + item.getStatusLabel();
        }
        return switch (changeType) {
            case "new" -> prefix + " 出现异常(" + item.getStatusLabel() + ")";
            case "changed" -> prefix + " 状态变化 "
                    + statusLabel(item.getPreviousStatus()) + "→" + item.getStatusLabel();
            case "persistent" -> prefix + " 持续异常(" + item.getStatusLabel() + ")";
            case "recovered" -> prefix + " 恢复为正常(原" + statusLabel(item.getPreviousStatus()) + ")";
            default -> prefix + " " + item.getStatusLabel();
        };
    }

    private static void appendSection(List<String> sections, String title, List<DutyLogChangeEntryVO> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        String content = entries.stream()
                .map(DutyLogClosureFormatter::formatChangeEntry)
                .collect(Collectors.joining("、"));
        sections.add("【" + title + "】" + content);
    }

    private static String targetLabel(String targetType, String targetName) {
        if ("site".equals(targetType)) {
            return "站点【" + targetName + "】";
        }
        return "模块【" + targetName + "】";
    }
}
