package com.oceanduty.module.duty;

import com.oceanduty.constant.DutyLogChangeTypeConst;
import com.oceanduty.constant.DutyLogEventTimeTypeConst;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import com.oceanduty.module.duty.domain.DutyMonitorSnapshotItemVO;
import com.oceanduty.module.monitor.MonitorEventTimeResolver;
import com.oceanduty.module.monitor.domain.DashboardVO;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 为快照差异项补充业务事件时间（故障/恢复）
 */
public final class DutyLogEventTimeEnricher {

    private static final String TARGET_SITE = "site";
    private static final String TARGET_MODULE = "module";

    private DutyLogEventTimeEnricher() {
    }

    public static void enrich(DutyLogSnapshotDiffUtil.DiffResult diff,
                              DashboardVO dashboard,
                              List<DutyLogItemEntity> previousItems,
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
                row.setEventTime(resolveRecoverTime(item, siteMap, moduleMap));
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

    private static LocalDateTime resolveFaultTime(DutyMonitorSnapshotItemVO item,
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

    private static LocalDateTime resolveRecoverTime(DutyMonitorSnapshotItemVO item,
                                                    Map<Long, MonitorSiteVO> siteMap,
                                                    Map<Long, MonitorModuleVO> moduleMap) {
        if (TARGET_SITE.equals(item.getTargetType())) {
            MonitorSiteVO site = siteMap.get(item.getTargetId());
            return MonitorEventTimeResolver.resolveSiteRecoverTime(site);
        }
        if (TARGET_MODULE.equals(item.getTargetType())) {
            MonitorModuleVO module = moduleMap.get(item.getTargetId());
            return MonitorEventTimeResolver.resolveModuleRecoverTime(module);
        }
        return null;
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
