package com.oceanduty.module.duty;

import com.oceanduty.constant.DutyLogChangeTypeConst;
import com.oceanduty.constant.DutyLogEventTimeTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.duty.domain.DutyIncidentVO;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import com.oceanduty.module.duty.domain.DutyMonitorSnapshotItemVO;
import com.oceanduty.module.monitor.MonitorEventTimeResolver;
import com.oceanduty.module.monitor.MonitorModuleCheckService;
import com.oceanduty.module.monitor.domain.DashboardVO;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 值班日志业务事件时间（故障/恢复）推算与回填
 */
@Service
@RequiredArgsConstructor
public class DutyLogEventTimeService {

    private static final String TARGET_SITE = "site";
    private static final String TARGET_MODULE = "module";

    private final MonitorModuleCheckService monitorModuleCheckService;

    public void enrichSnapshotDiff(DutyLogSnapshotDiffUtil.DiffResult diff,
                                   DashboardVO dashboard,
                                   List<DutyLogItemEntity> previousItems,
                                   LocalDate dutyDate,
                                   LocalDateTime now) {
        if (diff == null || diff.getItems() == null) {
            return;
        }
        Map<Long, MonitorSiteVO> siteMap = buildSiteMap(dashboard);
        Map<Long, MonitorModuleVO> moduleMap = buildModuleMap(dashboard);
        Map<String, DutyLogItemEntity> previousMap = buildPreviousMap(previousItems);

        for (DutyLogSnapshotDiffUtil.SnapshotItemWithChange row : diff.getItems()) {
            DutyMonitorSnapshotItemVO item = row.getItem();
            if (item == null) {
                continue;
            }
            String changeType = row.getChangeType();
            if (DutyLogChangeTypeConst.RECOVERED.equals(changeType)) {
                row.setEventTimeType(DutyLogEventTimeTypeConst.RECOVER);
                row.setEventTime(resolveRecoverTime(item, siteMap, moduleMap, dutyDate));
                continue;
            }
            row.setEventTimeType(DutyLogEventTimeTypeConst.FAULT);
            DutyLogItemEntity previous = previousMap.get(item.getTargetKey());
            if (previous != null
                    && DutyLogEventTimeTypeConst.FAULT.equals(previous.getEventTimeType())
                    && previous.getEventTime() != null
                    && (DutyLogChangeTypeConst.PERSISTENT.equals(changeType)
                    || DutyLogChangeTypeConst.CHANGED.equals(changeType))) {
                row.setEventTime(previous.getEventTime());
                continue;
            }
            row.setEventTime(resolveFaultTime(item, siteMap, moduleMap, now));
        }
    }

    public void backfillItemEventTimes(List<DutyLogItemEntity> items,
                                       DashboardVO dashboard,
                                       LocalDate dutyDate) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Long, MonitorSiteVO> siteMap = buildSiteMap(dashboard);
        Map<Long, MonitorModuleVO> moduleMap = buildModuleMap(dashboard);
        LocalDateTime now = LocalDateTime.now();
        for (DutyLogItemEntity item : items) {
            if (item.getEventTime() != null) {
                continue;
            }
            if (DutyLogChangeTypeConst.RECOVERED.equals(item.getChangeType())) {
                item.setEventTimeType(DutyLogEventTimeTypeConst.RECOVER);
                DutyMonitorSnapshotItemVO snapshotItem = toSnapshotItem(item);
                item.setEventTime(resolveRecoverTime(snapshotItem, siteMap, moduleMap, dutyDate));
                continue;
            }
            if (item.getStatus() != null && !MonitorStatusConst.NORMAL.equals(item.getStatus())) {
                item.setEventTimeType(DutyLogEventTimeTypeConst.FAULT);
                DutyMonitorSnapshotItemVO snapshotItem = toSnapshotItem(item);
                item.setEventTime(resolveFaultTime(snapshotItem, siteMap, moduleMap, now));
            }
        }
    }

    public void patchIncidentsWithItemEventTimes(List<DutyIncidentVO> incidents,
                                                 List<DutyLogItemEntity> items,
                                                 Long logId) {
        if (incidents == null || incidents.isEmpty() || items == null || items.isEmpty()) {
            return;
        }
        Map<String, DutyLogItemEntity> itemMap = new HashMap<>();
        for (DutyLogItemEntity item : items) {
            itemMap.put(item.getTargetKey(), item);
        }
        for (DutyIncidentVO incident : incidents) {
            DutyLogItemEntity item = itemMap.get(incident.getTargetKey());
            if (item == null || item.getEventTime() == null) {
                continue;
            }
            if (logId != null && logId.equals(incident.getRecoverLogId())
                    && DutyLogEventTimeTypeConst.RECOVER.equals(item.getEventTimeType())) {
                incident.setRecoveredTime(item.getEventTime());
                incident.setLifecycleText(buildRecoveredLifecycle(incident, item));
            }
            if (logId != null && logId.equals(incident.getFirstLogId())
                    && DutyLogEventTimeTypeConst.FAULT.equals(item.getEventTimeType())) {
                incident.setFirstFaultTime(item.getEventTime());
                incident.setLifecycleText(buildOpenLifecycle(incident, item));
            }
        }
    }

    private LocalDateTime resolveFaultTime(DutyMonitorSnapshotItemVO item,
                                           Map<Long, MonitorSiteVO> siteMap,
                                           Map<Long, MonitorModuleVO> moduleMap,
                                           LocalDateTime now) {
        if (TARGET_SITE.equals(item.getTargetType())) {
            MonitorSiteVO site = siteMap.get(item.getTargetId());
            return MonitorEventTimeResolver.resolveSiteFaultTime(site, now);
        }
        if (TARGET_MODULE.equals(item.getTargetType())) {
            MonitorModuleVO module = moduleMap.get(item.getTargetId());
            return MonitorEventTimeResolver.resolveModuleFaultTime(module, now);
        }
        return now;
    }

    private LocalDateTime resolveRecoverTime(DutyMonitorSnapshotItemVO item,
                                             Map<Long, MonitorSiteVO> siteMap,
                                             Map<Long, MonitorModuleVO> moduleMap,
                                             LocalDate dutyDate) {
        if (TARGET_SITE.equals(item.getTargetType())) {
            MonitorSiteVO site = siteMap.get(item.getTargetId());
            return MonitorEventTimeResolver.resolveSiteRecoverTime(site);
        }
        if (TARGET_MODULE.equals(item.getTargetType())) {
            return resolveModuleRecoverTime(item.getTargetId(), moduleMap.get(item.getTargetId()), dutyDate);
        }
        return null;
    }

    private LocalDateTime resolveModuleRecoverTime(Long moduleId,
                                                   MonitorModuleVO module,
                                                   LocalDate dutyDate) {
        if (moduleId == null) {
            return null;
        }
        LocalDateTime liveOnDate = dutyDate == null
                ? null
                : monitorModuleCheckService.fetchDataUpdateTimeOnDate(moduleId, dutyDate);
        if (liveOnDate != null) {
            return liveOnDate;
        }
        LocalDateTime liveLatest = monitorModuleCheckService.fetchLatestDataUpdateTime(moduleId);
        if (liveLatest != null && dutyDate != null && liveLatest.toLocalDate().equals(dutyDate)) {
            return liveLatest;
        }
        if (module != null && module.getUpdateTime() != null) {
            if (dutyDate == null || module.getUpdateTime().toLocalDate().equals(dutyDate)) {
                return module.getUpdateTime();
            }
        }
        return liveLatest;
    }

    private static DutyMonitorSnapshotItemVO toSnapshotItem(DutyLogItemEntity item) {
        return DutyMonitorSnapshotItemVO.builder()
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetKey(item.getTargetKey())
                .targetName(item.getTargetName())
                .category(item.getCategory())
                .checkType(item.getCheckType())
                .status(item.getStatus())
                .statusLabel(item.getStatusLabel())
                .build();
    }

    private static String buildRecoveredLifecycle(DutyIncidentVO incident, DutyLogItemEntity item) {
        LocalDateTime faultTime = incident.getFirstFaultTime() != null
                ? incident.getFirstFaultTime()
                : incident.getFirstSeenTime();
        String prefix = targetLabel(item);
        return prefix + "："
                + DutyLogClosureFormatter.formatEventTime(faultTime) + " "
                + DutyLogClosureFormatter.statusLabel(item.getPreviousStatus()) + " → "
                + DutyLogClosureFormatter.formatEventTime(item.getEventTime()) + " 已恢复";
    }

    private static String buildOpenLifecycle(DutyIncidentVO incident, DutyLogItemEntity item) {
        String prefix = targetLabel(item);
        return prefix + "："
                + DutyLogClosureFormatter.formatEventTime(item.getEventTime()) + " "
                + DutyLogClosureFormatter.statusLabel(item.getStatus()) + " → 进行中";
    }

    private static String targetLabel(DutyLogItemEntity item) {
        if (TARGET_SITE.equals(item.getTargetType())) {
            return "站点【" + item.getTargetName() + "】";
        }
        return "模块【" + item.getTargetName() + "】";
    }

    private static Map<Long, MonitorSiteVO> buildSiteMap(DashboardVO dashboard) {
        Map<Long, MonitorSiteVO> map = new HashMap<>();
        if (dashboard == null || dashboard.getSites() == null) {
            return map;
        }
        for (MonitorSiteVO site : dashboard.getSites()) {
            map.put(site.getId(), site);
        }
        return map;
    }

    private static Map<Long, MonitorModuleVO> buildModuleMap(DashboardVO dashboard) {
        Map<Long, MonitorModuleVO> map = new HashMap<>();
        if (dashboard == null || dashboard.getModules() == null) {
            return map;
        }
        for (MonitorModuleVO module : dashboard.getModules()) {
            map.put(module.getId(), module);
        }
        return map;
    }

    private static Map<String, DutyLogItemEntity> buildPreviousMap(List<DutyLogItemEntity> previousItems) {
        Map<String, DutyLogItemEntity> map = new HashMap<>();
        if (previousItems == null) {
            return map;
        }
        for (DutyLogItemEntity item : previousItems) {
            if (DutyLogChangeTypeConst.RECOVERED.equals(item.getChangeType())) {
                continue;
            }
            map.put(item.getTargetKey(), item);
        }
        return map;
    }
}
