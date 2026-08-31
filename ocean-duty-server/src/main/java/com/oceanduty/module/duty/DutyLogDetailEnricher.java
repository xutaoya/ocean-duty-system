package com.oceanduty.module.duty;

import com.oceanduty.constant.DutyIncidentStatusConst;
import com.oceanduty.constant.DutyLogChangeTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.duty.domain.DutyIncidentVO;
import com.oceanduty.module.duty.domain.DutyLogChangeEntryVO;
import com.oceanduty.module.duty.domain.DutyLogChangeSummaryVO;
import com.oceanduty.module.duty.domain.DutyLogEntity;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import com.oceanduty.module.monitor.MonitorEffectiveStatusUtil;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 旧日志详情补全（闭环字段、事件、摘要）
 */
public final class DutyLogDetailEnricher {

    private DutyLogDetailEnricher() {
    }

    public static void normalizeItems(List<DutyLogItemEntity> items) {
        for (DutyLogItemEntity item : items) {
            if (StringUtils.hasText(item.getChangeType())) {
                continue;
            }
            if (MonitorStatusConst.NORMAL.equals(item.getStatus())) {
                item.setChangeType(DutyLogChangeTypeConst.RECOVERED);
            } else {
                item.setChangeType(DutyLogChangeTypeConst.NEW);
            }
            if (!StringUtils.hasText(item.getStatusLabel())) {
                item.setStatusLabel(DutyLogClosureFormatter.statusLabel(item.getStatus()));
            }
            if (!StringUtils.hasText(item.getStateToken())) {
                item.setStateToken(DutyLogSnapshotDiffUtil.buildStateToken(item.getTargetKey(), item.getStatus()));
            }
        }
    }

    public static DutyLogChangeSummaryVO buildChangeSummary(List<DutyLogItemEntity> items) {
        List<DutyLogChangeEntryVO> recovered = new ArrayList<>();
        List<DutyLogChangeEntryVO> newAbnormals = new ArrayList<>();
        List<DutyLogChangeEntryVO> changed = new ArrayList<>();
        List<DutyLogChangeEntryVO> persistent = new ArrayList<>();
        for (DutyLogItemEntity item : items) {
            DutyLogChangeEntryVO entry = toChangeEntry(item);
            switch (item.getChangeType()) {
                case DutyLogChangeTypeConst.RECOVERED -> recovered.add(entry);
                case DutyLogChangeTypeConst.CHANGED -> changed.add(entry);
                case DutyLogChangeTypeConst.PERSISTENT -> persistent.add(entry);
                default -> newAbnormals.add(entry);
            }
        }
        return DutyLogChangeSummaryVO.builder()
                .recovered(recovered)
                .newAbnormals(newAbnormals)
                .changed(changed)
                .persistent(persistent)
                .build();
    }

    public static int resolveAbnormalCount(DutyLogEntity entity) {
        if (entity.getAbnormalCount() != null && entity.getAbnormalCount() > 0) {
            return entity.getAbnormalCount();
        }
        return countFromStatusSummary(entity.getSiteStatus(), entity.getModuleStatus());
    }

    private static int countFromStatusSummary(String siteStatus, String moduleStatus) {
        int count = 0;
        count += countStatusItems(siteStatus, "异常站点:");
        count += countStatusItems(moduleStatus, "异常模块:");
        return count;
    }

    private static int countStatusItems(String status, String prefix) {
        if (!StringUtils.hasText(status) || status.contains("全部正常") || !status.startsWith(prefix)) {
            return 0;
        }
        String body = status.substring(prefix.length()).trim();
        if (!StringUtils.hasText(body)) {
            return 0;
        }
        return (int) java.util.Arrays.stream(body.split("、"))
                .filter(StringUtils::hasText)
                .count();
    }

    public static String resolveClosureSummary(DutyLogEntity entity, DutyLogChangeSummaryVO changeSummary) {
        if (StringUtils.hasText(entity.getClosureSummary())) {
            return entity.getClosureSummary();
        }
        if (changeSummary != null) {
            String summary = DutyLogClosureFormatter.buildClosureSummary(changeSummary);
            if (StringUtils.hasText(summary)) {
                return summary;
            }
        }
        if (StringUtils.hasText(entity.getModuleStatus()) && !"全部正常".equals(entity.getModuleStatus())) {
            return entity.getModuleStatus();
        }
        if ("snapshot".equals(entity.getLogSource())) {
            return "全部正常，无异常变更";
        }
        return null;
    }

    public static List<DutyIncidentVO> enrichIncidents(DutyLogEntity entity,
                                                       List<DutyLogItemEntity> items,
                                                       List<DutyIncidentVO> incidents) {
        if (incidents != null && !incidents.isEmpty()) {
            return incidents;
        }
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<DutyIncidentVO> synthesized = new ArrayList<>();
        LocalDateTime seenTime = entity.getDutyTime();
        for (DutyLogItemEntity item : items) {
            if (DutyLogChangeTypeConst.RECOVERED.equals(item.getChangeType())) {
                synthesized.add(buildRecoveredIncident(entity, item, seenTime));
                continue;
            }
            if (MonitorEffectiveStatusUtil.isAbnormal(item.getStatus())) {
                synthesized.add(buildOpenIncident(entity, item, seenTime));
            }
        }
        return synthesized;
    }

    private static DutyIncidentVO buildOpenIncident(DutyLogEntity entity,
                                                    DutyLogItemEntity item,
                                                    LocalDateTime seenTime) {
        LocalDateTime faultTime = item.getEventTime() != null ? item.getEventTime() : seenTime;
        String lifecycle = targetLabel(item) + "："
                + DutyLogClosureFormatter.formatTime(faultTime) + " "
                + DutyLogClosureFormatter.statusLabel(item.getStatus()) + " → 进行中";
        return DutyIncidentVO.builder()
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetKey(item.getTargetKey())
                .targetName(item.getTargetName())
                .category(item.getCategory())
                .checkType(item.getCheckType())
                .dutyDate(entity.getDutyDate() != null ? entity.getDutyDate() : seenTime.toLocalDate())
                .incidentStatus(DutyIncidentStatusConst.OPEN)
                .firstStatus(item.getStatus())
                .lastStatus(item.getStatus())
                .firstStatusLabel(DutyLogClosureFormatter.statusLabel(item.getStatus()))
                .lastStatusLabel(DutyLogClosureFormatter.statusLabel(item.getStatus()))
                .firstLogId(entity.getId())
                .lastLogId(entity.getId())
                .firstSeenTime(seenTime)
                .firstFaultTime(faultTime)
                .lastSeenTime(seenTime)
                .lifecycleText(lifecycle)
                .eventRole("start")
                .eventRoleLabel(DutyLogClosureFormatter.eventRoleLabel("start"))
                .build();
    }

    private static DutyIncidentVO buildRecoveredIncident(DutyLogEntity entity,
                                                         DutyLogItemEntity item,
                                                         LocalDateTime seenTime) {
        LocalDateTime recoverTime = item.getEventTime() != null ? item.getEventTime() : seenTime;
        String lifecycle = targetLabel(item) + "："
                + DutyLogClosureFormatter.formatTime(recoverTime) + " "
                + DutyLogClosureFormatter.statusLabel(item.getPreviousStatus()) + " → "
                + DutyLogClosureFormatter.formatTime(recoverTime) + " 已恢复";
        return DutyIncidentVO.builder()
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetKey(item.getTargetKey())
                .targetName(item.getTargetName())
                .category(item.getCategory())
                .checkType(item.getCheckType())
                .dutyDate(entity.getDutyDate() != null ? entity.getDutyDate() : seenTime.toLocalDate())
                .incidentStatus(DutyIncidentStatusConst.RECOVERED)
                .firstStatus(item.getPreviousStatus())
                .lastStatus(MonitorStatusConst.NORMAL)
                .firstStatusLabel(DutyLogClosureFormatter.statusLabel(item.getPreviousStatus()))
                .lastStatusLabel(DutyLogClosureFormatter.statusLabel(MonitorStatusConst.NORMAL))
                .firstLogId(entity.getId())
                .lastLogId(entity.getId())
                .recoverLogId(entity.getId())
                .firstSeenTime(seenTime)
                .lastSeenTime(seenTime)
                .recoveredTime(recoverTime)
                .lifecycleText(lifecycle)
                .eventRole("recover")
                .eventRoleLabel(DutyLogClosureFormatter.eventRoleLabel("recover"))
                .build();
    }

    private static DutyLogChangeEntryVO toChangeEntry(DutyLogItemEntity item) {
        return DutyLogChangeEntryVO.builder()
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetKey(item.getTargetKey())
                .targetName(item.getTargetName())
                .category(item.getCategory())
                .checkType(item.getCheckType())
                .status(item.getStatus())
                .previousStatus(item.getPreviousStatus())
                .statusLabel(item.getStatusLabel())
                .changeType(item.getChangeType())
                .stateToken(item.getStateToken())
                .build();
    }

    private static String targetLabel(DutyLogItemEntity item) {
        if ("site".equals(item.getTargetType())) {
            return "站点【" + item.getTargetName() + "】";
        }
        return "模块【" + item.getTargetName() + "】";
    }
}
