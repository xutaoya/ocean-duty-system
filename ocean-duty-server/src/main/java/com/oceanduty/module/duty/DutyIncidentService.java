package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.DutyIncidentStatusConst;
import com.oceanduty.constant.DutyLogChangeTypeConst;
import com.oceanduty.module.duty.domain.DutyIncidentEntity;
import com.oceanduty.module.duty.domain.DutyIncidentVO;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班异常事件生命周期
 */
@Service
@RequiredArgsConstructor
public class DutyIncidentService {

    private final DutyIncidentDao dutyIncidentDao;
    private final ObjectMapper objectMapper;

    public void syncIncidents(LocalDate dutyDate,
                               Long logId,
                               LocalDateTime seenTime,
                               List<DutyLogSnapshotDiffUtil.SnapshotItemWithChange> items) {
        for (DutyLogSnapshotDiffUtil.SnapshotItemWithChange row : items) {
            String changeType = row.getChangeType();
            if (DutyLogChangeTypeConst.NEW.equals(changeType)) {
                openIncident(dutyDate, logId, seenTime, row);
            } else if (DutyLogChangeTypeConst.CHANGED.equals(changeType)
                    || DutyLogChangeTypeConst.PERSISTENT.equals(changeType)) {
                touchIncident(dutyDate, logId, seenTime, row);
            } else if (DutyLogChangeTypeConst.RECOVERED.equals(changeType)) {
                recoverIncident(dutyDate, logId, seenTime, row);
            }
        }
    }

    public List<DutyIncidentVO> listByLogId(Long logId) {
        return dutyIncidentDao.selectList(new LambdaQueryWrapper<DutyIncidentEntity>()
                        .and(wrapper -> wrapper
                                .eq(DutyIncidentEntity::getFirstLogId, logId)
                                .or()
                                .eq(DutyIncidentEntity::getLastLogId, logId)
                                .or()
                                .eq(DutyIncidentEntity::getRecoverLogId, logId))
                        .orderByAsc(DutyIncidentEntity::getFirstSeenTime))
                .stream()
                .map(entity -> toVO(entity, logId))
                .collect(Collectors.toList());
    }

    public List<DutyIncidentVO> listByDutyDate(LocalDate dutyDate) {
        return dutyIncidentDao.selectList(new LambdaQueryWrapper<DutyIncidentEntity>()
                        .eq(DutyIncidentEntity::getDutyDate, dutyDate)
                        .orderByAsc(DutyIncidentEntity::getFirstSeenTime))
                .stream()
                .map(entity -> toVO(entity, null))
                .collect(Collectors.toList());
    }

    private void openIncident(LocalDate dutyDate,
                              Long logId,
                              LocalDateTime seenTime,
                              DutyLogSnapshotDiffUtil.SnapshotItemWithChange row) {
        LocalDateTime faultTime = resolveFaultTime(row, seenTime);
        DutyIncidentEntity entity = DutyIncidentEntity.builder()
                .targetType(row.getItem().getTargetType())
                .targetId(row.getItem().getTargetId())
                .targetKey(row.getItem().getTargetKey())
                .targetName(row.getItem().getTargetName())
                .category(row.getItem().getCategory())
                .checkType(row.getItem().getCheckType())
                .dutyDate(dutyDate)
                .incidentStatus(DutyIncidentStatusConst.OPEN)
                .firstStatus(row.getItem().getStatus())
                .lastStatus(row.getItem().getStatus())
                .firstLogId(logId)
                .lastLogId(logId)
                .firstSeenTime(seenTime)
                .firstFaultTime(faultTime)
                .lastSeenTime(seenTime)
                .build();
        dutyIncidentDao.insert(entity);
    }

    private void touchIncident(LocalDate dutyDate,
                               Long logId,
                               LocalDateTime seenTime,
                               DutyLogSnapshotDiffUtil.SnapshotItemWithChange row) {
        DutyIncidentEntity open = findOpenIncident(dutyDate, row.getItem().getTargetKey());
        if (open == null) {
            openIncident(dutyDate, logId, seenTime, row);
            return;
        }
        open.setLastLogId(logId);
        open.setLastStatus(row.getItem().getStatus());
        open.setLastSeenTime(seenTime);
        dutyIncidentDao.updateById(open);
    }

    private void recoverIncident(LocalDate dutyDate,
                                 Long logId,
                                 LocalDateTime seenTime,
                                 DutyLogSnapshotDiffUtil.SnapshotItemWithChange row) {
        DutyIncidentEntity open = findOpenIncident(dutyDate, row.getItem().getTargetKey());
        if (open == null) {
            return;
        }
        LocalDateTime recoverTime = row.getEventTime() != null ? row.getEventTime() : seenTime;
        open.setIncidentStatus(DutyIncidentStatusConst.RECOVERED);
        open.setRecoverLogId(logId);
        open.setRecoveredTime(recoverTime);
        open.setLastLogId(logId);
        open.setLastStatus(1);
        open.setLastSeenTime(seenTime);
        dutyIncidentDao.updateById(open);
    }

    private LocalDateTime resolveFaultTime(DutyLogSnapshotDiffUtil.SnapshotItemWithChange row, LocalDateTime fallback) {
        if (row.getEventTime() != null) {
            return row.getEventTime();
        }
        return fallback;
    }

    private DutyIncidentEntity findOpenIncident(LocalDate dutyDate, String targetKey) {
        return dutyIncidentDao.selectOne(new LambdaQueryWrapper<DutyIncidentEntity>()
                .eq(DutyIncidentEntity::getDutyDate, dutyDate)
                .eq(DutyIncidentEntity::getTargetKey, targetKey)
                .eq(DutyIncidentEntity::getIncidentStatus, DutyIncidentStatusConst.OPEN)
                .orderByDesc(DutyIncidentEntity::getId)
                .last("LIMIT 1"));
    }

    private DutyIncidentVO toVO(DutyIncidentEntity entity, Long logId) {
        String eventRole = DutyLogClosureFormatter.resolveEventRole(logId, entity);
        return DutyIncidentVO.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .targetKey(entity.getTargetKey())
                .targetName(entity.getTargetName())
                .category(entity.getCategory())
                .checkType(entity.getCheckType())
                .dutyDate(entity.getDutyDate())
                .incidentStatus(entity.getIncidentStatus())
                .firstStatus(entity.getFirstStatus())
                .lastStatus(entity.getLastStatus())
                .firstStatusLabel(DutyLogClosureFormatter.statusLabel(entity.getFirstStatus()))
                .lastStatusLabel(DutyLogClosureFormatter.statusLabel(entity.getLastStatus()))
                .firstLogId(entity.getFirstLogId())
                .lastLogId(entity.getLastLogId())
                .recoverLogId(entity.getRecoverLogId())
                .firstSeenTime(entity.getFirstSeenTime())
                .firstFaultTime(entity.getFirstFaultTime())
                .lastSeenTime(entity.getLastSeenTime())
                .recoveredTime(entity.getRecoveredTime())
                .lifecycleText(DutyLogClosureFormatter.formatIncidentLifecycle(entity))
                .eventRole(eventRole)
                .eventRoleLabel(DutyLogClosureFormatter.eventRoleLabel(eventRole))
                .build();
    }

    String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseCodeConst.ERROR_SYSTEM, "JSON 序列化失败");
        }
    }

    <T> T readJson(String json, Class<T> type) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
