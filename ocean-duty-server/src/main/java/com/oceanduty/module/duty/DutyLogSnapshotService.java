package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.RequestUser;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.DutyLogActionTypeConst;
import com.oceanduty.constant.DutyLogChangeTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.duty.domain.DutyLogEntity;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import com.oceanduty.module.duty.domain.DutyLogRecordResultVO;
import com.oceanduty.module.duty.domain.DutyLogSnapshotStatusVO;
import com.oceanduty.module.duty.domain.DutyMonitorSnapshotItemVO;
import com.oceanduty.module.duty.domain.DutyMonitorSnapshotVO;
import com.oceanduty.module.login.SysUserDao;
import com.oceanduty.module.login.domain.SysUserEntity;
import com.oceanduty.module.monitor.MonitorEffectiveStatusUtil;
import com.oceanduty.module.monitor.MonitorQueryService;
import com.oceanduty.module.monitor.domain.DashboardVO;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
import com.oceanduty.util.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘监控快照日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DutyLogSnapshotService {

    private static final String ACTION_RECORD = "record";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DONE = "done";
    private static final String TARGET_SITE = "site";
    private static final String TARGET_MODULE = "module";

    private final DutyLogDao dutyLogDao;
    private final DutyLogItemDao dutyLogItemDao;
    private final DutyIncidentService dutyIncidentService;
    private final MonitorQueryService monitorQueryService;
    private final SysUserDao sysUserDao;
    private final ObjectMapper objectMapper;

    public ResponseDTO<DutyLogSnapshotStatusVO> getSnapshotStatus() {
        LocalDate today = LocalDate.now();
        DashboardVO dashboard = monitorQueryService.getDashboard();
        DutyMonitorSnapshotVO snapshot = buildSnapshot(dashboard);
        String fingerprint = computeFingerprint(snapshot.getItems());
        DutyLogEntity lastTodayLog = findLatestLogOfDay(today);
        return ResponseDTO.succ(resolveStatus(today, snapshot, fingerprint, lastTodayLog));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<DutyLogRecordResultVO> recordSnapshot() {
        LocalDate today = LocalDate.now();
        DashboardVO dashboard = monitorQueryService.getDashboard();
        DutyMonitorSnapshotVO snapshot = buildSnapshot(dashboard);
        String fingerprint = computeFingerprint(snapshot.getItems());
        DutyLogEntity lastTodayLog = findLatestLogOfDay(today);

        String action;
        if (lastTodayLog == null) {
            action = ACTION_RECORD;
        } else if (fingerprint.equals(lastTodayLog.getStateFingerprint())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "当前监控状态与最近一条日志一致，无需重复记录");
        } else {
            action = ACTION_UPDATE;
        }

        List<DutyLogItemEntity> previousAbnormalItems = loadPreviousAbnormalItems(lastTodayLog);
        DutyLogSnapshotDiffUtil.DiffResult diff = DutyLogSnapshotDiffUtil.diff(snapshot.getItems(), previousAbnormalItems);

        RequestUser requestUser = requireRequestUser();
        String userName = resolveRealName(requestUser);
        LocalDateTime now = LocalDateTime.now();

        DutyLogEntity entity = DutyLogEntity.builder()
                .userId(requestUser.getUserId())
                .userName(userName)
                .dutyTime(now)
                .dutyDate(today)
                .siteStatus(buildSiteSummary(snapshot))
                .moduleStatus(buildModuleSummary(snapshot))
                .logSource("snapshot")
                .actionType(action)
                .previousLogId(lastTodayLog == null ? null : lastTodayLog.getId())
                .stateFingerprint(fingerprint)
                .changeSummary(writeJson(diff.getSummary()))
                .abnormalCount(diff.getAbnormalCount())
                .newAbnormalCount(diff.getNewAbnormalCount())
                .changedCount(diff.getChangedCount())
                .recoveredCount(diff.getRecoveredCount())
                .snapshotJson(writeSnapshotJson(snapshot))
                .build();
        dutyLogDao.insert(entity);
        persistItems(entity.getId(), diff.getItems());
        dutyIncidentService.syncIncidents(today, entity.getId(), now, diff.getItems());

        return ResponseDTO.succ(DutyLogRecordResultVO.builder()
                .logId(entity.getId())
                .userName(userName)
                .dutyTime(now)
                .abnormalSiteCount(countByType(snapshot, TARGET_SITE))
                .abnormalModuleCount(countByType(snapshot, TARGET_MODULE))
                .action(action)
                .build());
    }

    private DutyLogSnapshotStatusVO resolveStatus(LocalDate today,
                                                  DutyMonitorSnapshotVO snapshot,
                                                  String fingerprint,
                                                  DutyLogEntity lastTodayLog) {
        int siteCount = countByType(snapshot, TARGET_SITE);
        int moduleCount = countByType(snapshot, TARGET_MODULE);
        if (lastTodayLog == null) {
            return DutyLogSnapshotStatusVO.builder()
                    .action(ACTION_RECORD)
                    .buttonLabel("记录日志")
                    .clickable(true)
                    .abnormalSiteCount(siteCount)
                    .abnormalModuleCount(moduleCount)
                    .dutyDate(today.toString())
                    .build();
        }
        if (fingerprint.equals(lastTodayLog.getStateFingerprint())) {
            return DutyLogSnapshotStatusVO.builder()
                    .action(ACTION_DONE)
                    .buttonLabel("已记录")
                    .clickable(false)
                    .abnormalSiteCount(siteCount)
                    .abnormalModuleCount(moduleCount)
                    .lastLogId(lastTodayLog.getId())
                    .lastLogTime(lastTodayLog.getDutyTime())
                    .dutyDate(today.toString())
                    .build();
        }
        return DutyLogSnapshotStatusVO.builder()
                .action(ACTION_UPDATE)
                .buttonLabel("更新日志")
                .clickable(true)
                .abnormalSiteCount(siteCount)
                .abnormalModuleCount(moduleCount)
                .lastLogId(lastTodayLog.getId())
                .lastLogTime(lastTodayLog.getDutyTime())
                .dutyDate(today.toString())
                .build();
    }

    private DutyMonitorSnapshotVO buildSnapshot(DashboardVO dashboard) {
        List<DutyMonitorSnapshotItemVO> items = new ArrayList<>();
        if (dashboard.getSites() != null) {
            dashboard.getSites().stream()
                    .filter(site -> MonitorEffectiveStatusUtil.isAbnormal(site.getStatus()))
                    .map(this::toSiteItem)
                    .forEach(items::add);
        }
        if (dashboard.getModules() != null) {
            dashboard.getModules().stream()
                    .filter(module -> MonitorEffectiveStatusUtil.isAbnormal(
                            MonitorEffectiveStatusUtil.resolveEffectiveStatus(module)))
                    .map(this::toModuleItem)
                    .forEach(items::add);
        }
        items.sort(Comparator.comparing(DutyMonitorSnapshotItemVO::getTargetKey));
        return DutyMonitorSnapshotVO.builder()
                .capturedAt(LocalDateTime.now())
                .items(items)
                .build();
    }

    private DutyMonitorSnapshotItemVO toSiteItem(MonitorSiteVO site) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("siteUrl", site.getSiteUrl());
        detail.put("siteType", site.getSiteType());
        detail.put("httpStatus", site.getHttpStatus());
        detail.put("responseTime", site.getResponseTime());
        detail.put("errorMessage", site.getErrorMessage());
        detail.put("lastCheckTime", site.getLastCheckTime());
        return DutyMonitorSnapshotItemVO.builder()
                .targetType(TARGET_SITE)
                .targetId(site.getId())
                .targetKey(TARGET_SITE + ":" + site.getId())
                .targetName(site.getSiteName())
                .category(site.getSiteType())
                .status(site.getStatus())
                .statusLabel(resolveStatusLabel(site.getStatus()))
                .detail(detail)
                .build();
    }

    private DutyMonitorSnapshotItemVO toModuleItem(MonitorModuleVO module) {
        Integer effectiveStatus = MonitorEffectiveStatusUtil.resolveEffectiveStatus(module);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("moduleCategory", module.getModuleCategory());
        detail.put("moduleCategoryName", module.getModuleCategoryName());
        detail.put("moduleGroup", module.getModuleGroup());
        detail.put("checkParam", module.getCheckParam());
        detail.put("updateTime", module.getUpdateTime());
        detail.put("expectedTime", module.getExpectedTime());
        detail.put("remark", module.getRemark());
        detail.put("alarmTitle", module.getAlarmTitle());
        detail.put("alarmCode", module.getAlarmCode());
        detail.put("alarmLevel", module.getAlarmLevel());
        detail.put("lastCheckTime", module.getLastCheckTime());
        detail.put("rawStatus", module.getStatus());
        return DutyMonitorSnapshotItemVO.builder()
                .targetType(TARGET_MODULE)
                .targetId(module.getId())
                .targetKey(TARGET_MODULE + ":" + module.getId())
                .targetName(module.getModuleName())
                .category(module.getModuleCategory())
                .checkType(module.getCheckType())
                .status(effectiveStatus)
                .statusLabel(resolveStatusLabel(effectiveStatus))
                .detail(detail)
                .build();
    }

    private void persistItems(Long logId, List<DutyLogSnapshotDiffUtil.SnapshotItemWithChange> items) {
        for (DutyLogSnapshotDiffUtil.SnapshotItemWithChange row : items) {
            DutyMonitorSnapshotItemVO item = row.getItem();
            DutyLogItemEntity entity = DutyLogItemEntity.builder()
                    .logId(logId)
                    .targetType(item.getTargetType())
                    .targetId(item.getTargetId())
                    .targetKey(item.getTargetKey())
                    .targetName(item.getTargetName())
                    .category(item.getCategory())
                    .checkType(item.getCheckType())
                    .status(item.getStatus())
                    .statusLabel(item.getStatusLabel())
                    .changeType(row.getChangeType())
                    .previousStatus(row.getPreviousStatus())
                    .stateToken(row.getStateToken())
                    .detailJson(writeDetailJson(item.getDetail()))
                    .build();
            dutyLogItemDao.insert(entity);
        }
    }

    private List<DutyLogItemEntity> loadPreviousAbnormalItems(DutyLogEntity lastTodayLog) {
        if (lastTodayLog == null) {
            return List.of();
        }
        return dutyLogItemDao.selectList(new LambdaQueryWrapper<DutyLogItemEntity>()
                        .eq(DutyLogItemEntity::getLogId, lastTodayLog.getId())
                        .ne(DutyLogItemEntity::getChangeType, DutyLogChangeTypeConst.RECOVERED)
                        .orderByAsc(DutyLogItemEntity::getId));
    }

    private DutyLogEntity findLatestLogOfDay(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.atTime(LocalTime.MAX);
        return dutyLogDao.selectOne(new LambdaQueryWrapper<DutyLogEntity>()
                .eq(DutyLogEntity::getLogSource, "snapshot")
                .ge(DutyLogEntity::getDutyTime, start)
                .le(DutyLogEntity::getDutyTime, end)
                .orderByDesc(DutyLogEntity::getDutyTime)
                .last("LIMIT 1"));
    }

    private String computeFingerprint(List<DutyMonitorSnapshotItemVO> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .map(item -> DutyLogSnapshotDiffUtil.buildStateToken(item.getTargetKey(), item.getStatus()))
                .collect(Collectors.joining("|"));
    }

    private String buildSiteSummary(DutyMonitorSnapshotVO snapshot) {
        String summary = snapshot.getItems().stream()
                .filter(item -> TARGET_SITE.equals(item.getTargetType()))
                .map(item -> item.getTargetName() + "(" + item.getStatusLabel() + ")")
                .collect(Collectors.joining("、"));
        if (!StringUtils.hasText(summary)) {
            return "全部正常";
        }
        return "异常站点: " + summary;
    }

    private String buildModuleSummary(DutyMonitorSnapshotVO snapshot) {
        String summary = snapshot.getItems().stream()
                .filter(item -> TARGET_MODULE.equals(item.getTargetType()))
                .map(item -> item.getTargetName() + "(" + item.getStatusLabel() + ")")
                .collect(Collectors.joining("、"));
        if (!StringUtils.hasText(summary)) {
            return "全部正常";
        }
        return "异常模块: " + summary;
    }

    private int countByType(DutyMonitorSnapshotVO snapshot, String targetType) {
        return (int) snapshot.getItems().stream()
                .filter(item -> targetType.equals(item.getTargetType()))
                .count();
    }

    private String resolveStatusLabel(Integer status) {
        if (MonitorStatusConst.ERROR.equals(status)) {
            return "异常";
        }
        if (MonitorStatusConst.WARNING.equals(status)) {
            return "警告";
        }
        return "正常";
    }

    private String resolveRealName(RequestUser requestUser) {
        if (requestUser.getUserId() != null) {
            SysUserEntity user = sysUserDao.selectById(requestUser.getUserId());
            if (user != null && StringUtils.hasText(user.getRealName())) {
                return user.getRealName();
            }
        }
        if (StringUtils.hasText(requestUser.getUsername())) {
            return requestUser.getUsername();
        }
        throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "值班人员不能为空");
    }

    private RequestUser requireRequestUser() {
        RequestUser requestUser = RequestUserContext.get();
        if (requestUser == null) {
            throw new BusinessException(ResponseCodeConst.NOT_LOGIN);
        }
        return requestUser;
    }

    private String writeSnapshotJson(DutyMonitorSnapshotVO snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseCodeConst.ERROR_SYSTEM, "快照序列化失败");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseCodeConst.ERROR_SYSTEM, "变更摘要序列化失败");
        }
    }

    private String writeDetailJson(Map<String, Object> detail) {
        if (detail == null || detail.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            log.warn("明细序列化失败: {}", e.getMessage());
            return null;
        }
    }
}
