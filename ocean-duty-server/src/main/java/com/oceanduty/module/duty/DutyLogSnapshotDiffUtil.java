package com.oceanduty.module.duty;

import com.oceanduty.constant.DutyLogChangeTypeConst;
import com.oceanduty.module.duty.domain.DutyLogChangeEntryVO;
import com.oceanduty.module.duty.domain.DutyLogChangeSummaryVO;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import com.oceanduty.module.duty.domain.DutyMonitorSnapshotItemVO;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 监控快照差异计算
 */
public final class DutyLogSnapshotDiffUtil {

    private DutyLogSnapshotDiffUtil() {
    }

    @Data
    @Builder
    public static class DiffResult {
        private DutyLogChangeSummaryVO summary;
        private List<SnapshotItemWithChange> items;
        private int abnormalCount;
        private int newAbnormalCount;
        private int changedCount;
        private int recoveredCount;
    }

    @Data
    @Builder
    public static class SnapshotItemWithChange {
        private DutyMonitorSnapshotItemVO item;
        private String changeType;
        private Integer previousStatus;
        private String stateToken;
        private LocalDateTime eventTime;
        private String eventTimeType;
    }

    public static DiffResult diff(List<DutyMonitorSnapshotItemVO> currentItems,
                                  List<DutyLogItemEntity> previousItems) {
        Map<String, DutyMonitorSnapshotItemVO> currentMap = toCurrentMap(currentItems);
        Map<String, DutyLogItemEntity> previousMap = toPreviousMap(previousItems);

        List<DutyLogChangeEntryVO> newAbnormals = new ArrayList<>();
        List<DutyLogChangeEntryVO> changed = new ArrayList<>();
        List<DutyLogChangeEntryVO> persistent = new ArrayList<>();
        List<DutyLogChangeEntryVO> recovered = new ArrayList<>();
        List<SnapshotItemWithChange> resultItems = new ArrayList<>();

        for (DutyMonitorSnapshotItemVO current : currentMap.values()) {
            DutyLogItemEntity previous = previousMap.get(current.getTargetKey());
            if (previous == null) {
                SnapshotItemWithChange row = SnapshotItemWithChange.builder()
                        .item(current)
                        .changeType(DutyLogChangeTypeConst.NEW)
                        .stateToken(buildStateToken(current.getTargetKey(), current.getStatus()))
                        .build();
                resultItems.add(row);
                newAbnormals.add(toEntry(row, null));
                continue;
            }
            if (previous.getStatus() != null && previous.getStatus().equals(current.getStatus())) {
                SnapshotItemWithChange row = SnapshotItemWithChange.builder()
                        .item(current)
                        .changeType(DutyLogChangeTypeConst.PERSISTENT)
                        .previousStatus(previous.getStatus())
                        .stateToken(buildStateToken(current.getTargetKey(), current.getStatus()))
                        .build();
                resultItems.add(row);
                persistent.add(toEntry(row, previous.getStatus()));
                continue;
            }
            SnapshotItemWithChange row = SnapshotItemWithChange.builder()
                    .item(current)
                    .changeType(DutyLogChangeTypeConst.CHANGED)
                    .previousStatus(previous.getStatus())
                    .stateToken(buildStateToken(current.getTargetKey(), current.getStatus()))
                    .build();
            resultItems.add(row);
            changed.add(toEntry(row, previous.getStatus()));
        }

        for (DutyLogItemEntity previous : previousMap.values()) {
            if (currentMap.containsKey(previous.getTargetKey())) {
                continue;
            }
            DutyMonitorSnapshotItemVO recoveredItem = DutyMonitorSnapshotItemVO.builder()
                    .targetType(previous.getTargetType())
                    .targetId(previous.getTargetId())
                    .targetKey(previous.getTargetKey())
                    .targetName(previous.getTargetName())
                    .category(previous.getCategory())
                    .checkType(previous.getCheckType())
                    .status(1)
                    .statusLabel("正常")
                    .build();
            SnapshotItemWithChange row = SnapshotItemWithChange.builder()
                    .item(recoveredItem)
                    .changeType(DutyLogChangeTypeConst.RECOVERED)
                    .previousStatus(previous.getStatus())
                    .stateToken(buildStateToken(previous.getTargetKey(), 1))
                    .build();
            resultItems.add(row);
            recovered.add(toEntry(row, previous.getStatus()));
        }

        DutyLogChangeSummaryVO summary = DutyLogChangeSummaryVO.builder()
                .newAbnormals(newAbnormals)
                .changed(changed)
                .recovered(recovered)
                .persistent(persistent)
                .build();

        return DiffResult.builder()
                .summary(summary)
                .items(resultItems)
                .abnormalCount(currentMap.size())
                .newAbnormalCount(newAbnormals.size())
                .changedCount(changed.size())
                .recoveredCount(recovered.size())
                .build();
    }

    public static String buildStateToken(String targetKey, Integer status) {
        return targetKey + ":" + status;
    }

    private static Map<String, DutyMonitorSnapshotItemVO> toCurrentMap(List<DutyMonitorSnapshotItemVO> currentItems) {
        Map<String, DutyMonitorSnapshotItemVO> map = new LinkedHashMap<>();
        if (currentItems == null) {
            return map;
        }
        for (DutyMonitorSnapshotItemVO item : currentItems) {
            map.put(item.getTargetKey(), item);
        }
        return map;
    }

    private static Map<String, DutyLogItemEntity> toPreviousMap(List<DutyLogItemEntity> previousItems) {
        Map<String, DutyLogItemEntity> map = new LinkedHashMap<>();
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

    private static DutyLogChangeEntryVO toEntry(SnapshotItemWithChange row, Integer previousStatus) {
        DutyMonitorSnapshotItemVO item = row.getItem();
        return DutyLogChangeEntryVO.builder()
                .targetType(item.getTargetType())
                .targetId(item.getTargetId())
                .targetKey(item.getTargetKey())
                .targetName(item.getTargetName())
                .category(item.getCategory())
                .checkType(item.getCheckType())
                .status(item.getStatus())
                .previousStatus(previousStatus != null ? previousStatus : row.getPreviousStatus())
                .statusLabel(item.getStatusLabel())
                .changeType(row.getChangeType())
                .stateToken(row.getStateToken())
                .build();
    }
}
