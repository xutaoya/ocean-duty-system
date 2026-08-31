package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.RequestUser;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.DutyLogActionTypeConst;
import com.oceanduty.module.duty.domain.DutyIncidentVO;
import com.oceanduty.module.duty.domain.DutyLogChangeSummaryVO;
import com.oceanduty.module.duty.domain.DutyLogDTO;
import com.oceanduty.module.duty.domain.DutyLogDetailVO;
import com.oceanduty.module.duty.domain.DutyLogEntity;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import com.oceanduty.module.duty.domain.DutyLogItemVO;
import com.oceanduty.module.duty.domain.DutyLogQueryDTO;
import com.oceanduty.module.duty.domain.DutyLogTimelineVO;
import com.oceanduty.module.duty.domain.DutyLogVO;
import com.oceanduty.util.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班日志服务
 */
@Service
@RequiredArgsConstructor
public class DutyLogService {

    private final DutyLogDao dutyLogDao;
    private final DutyLogItemDao dutyLogItemDao;
    private final DutyIncidentService dutyIncidentService;

    public ResponseDTO<PageResultVO<DutyLogVO>> queryDutyLog(DutyLogQueryDTO queryDTO) {
        LambdaQueryWrapper<DutyLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getUserName())) {
            wrapper.like(DutyLogEntity::getUserName, queryDTO.getUserName());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(DutyLogEntity::getDutyTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(DutyLogEntity::getDutyTime, queryDTO.getEndTime());
        }
        wrapper.orderByDesc(DutyLogEntity::getDutyTime);

        Page<DutyLogEntity> page = dutyLogDao.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<DutyLogVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return ResponseDTO.succ(PageResultVO.<DutyLogVO>builder()
                .total(page.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
                .list(list)
                .build());
    }

    public ResponseDTO<DutyLogDetailVO> getDutyLogDetail(Long id) {
        DutyLogEntity entity = dutyLogDao.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        List<DutyLogItemEntity> itemEntities = dutyLogItemDao.selectList(new LambdaQueryWrapper<DutyLogItemEntity>()
                        .eq(DutyLogItemEntity::getLogId, id)
                        .orderByAsc(DutyLogItemEntity::getId));
        DutyLogDetailEnricher.normalizeItems(itemEntities);

        DutyLogChangeSummaryVO changeSummary = dutyIncidentService.readJson(
                entity.getChangeSummary(), DutyLogChangeSummaryVO.class);
        if (changeSummary == null && !itemEntities.isEmpty()) {
            changeSummary = DutyLogDetailEnricher.buildChangeSummary(itemEntities);
        }

        List<DutyIncidentVO> incidents = DutyLogDetailEnricher.enrichIncidents(
                entity, itemEntities, dutyIncidentService.listByLogId(id));
        String closureSummary = DutyLogDetailEnricher.resolveClosureSummary(entity, changeSummary);

        List<DutyLogItemVO> items = itemEntities.stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
        return ResponseDTO.succ(DutyLogDetailVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .dutyTime(entity.getDutyTime())
                .dutyDate(entity.getDutyDate())
                .logSource(entity.getLogSource())
                .actionType(entity.getActionType())
                .previousLogId(entity.getPreviousLogId())
                .siteStatus(entity.getSiteStatus())
                .moduleStatus(entity.getModuleStatus())
                .problem(entity.getProblem())
                .solution(entity.getSolution())
                .recoverTime(entity.getRecoverTime())
                .stateFingerprint(entity.getStateFingerprint())
                .abnormalCount(entity.getAbnormalCount())
                .newAbnormalCount(entity.getNewAbnormalCount())
                .changedCount(entity.getChangedCount())
                .recoveredCount(entity.getRecoveredCount())
                .changeSummary(changeSummary)
                .closureSummary(closureSummary)
                .timeline(buildTimeline(itemEntities))
                .items(items)
                .incidents(incidents)
                .build());
    }

    public ResponseDTO<String> addDutyLog(DutyLogDTO dutyLogDTO) {
        DutyLogEntity entity = toEntity(dutyLogDTO);
        entity.setUserName(resolveUserName(dutyLogDTO.getUserName()));
        entity.setLogSource("manual");
        entity.setActionType(DutyLogActionTypeConst.MANUAL);
        if (entity.getDutyTime() != null) {
            entity.setDutyDate(entity.getDutyTime().toLocalDate());
        }
        dutyLogDao.insert(entity);
        return ResponseDTO.succ();
    }

    public ResponseDTO<String> updateDutyLog(DutyLogDTO dutyLogDTO) {
        if (dutyLogDTO.getId() == null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "日志ID不能为空");
        }
        DutyLogEntity exist = dutyLogDao.selectById(dutyLogDTO.getId());
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        DutyLogEntity entity = toEntity(dutyLogDTO);
        entity.setId(dutyLogDTO.getId());
        entity.setUserName(resolveUserName(dutyLogDTO.getUserName()));
        if (entity.getDutyTime() != null) {
            entity.setDutyDate(entity.getDutyTime().toLocalDate());
        }
        dutyLogDao.updateById(entity);
        return ResponseDTO.succ();
    }

    public ResponseDTO<String> deleteDutyLog(Long id) {
        DutyLogEntity exist = dutyLogDao.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        dutyLogDao.deleteById(id);
        return ResponseDTO.succ();
    }

    private String resolveUserName(String userName) {
        if (StringUtils.hasText(userName)) {
            return userName;
        }
        RequestUser requestUser = RequestUserContext.get();
        if (requestUser != null && StringUtils.hasText(requestUser.getUsername())) {
            return requestUser.getUsername();
        }
        throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "值班人员不能为空");
    }

    private DutyLogVO toVO(DutyLogEntity entity) {
        return DutyLogVO.builder()
                .id(entity.getId())
                .userName(entity.getUserName())
                .dutyTime(entity.getDutyTime())
                .dutyDate(entity.getDutyDate())
                .siteStatus(entity.getSiteStatus())
                .moduleStatus(entity.getModuleStatus())
                .problem(entity.getProblem())
                .solution(entity.getSolution())
                .recoverTime(entity.getRecoverTime())
                .logSource(entity.getLogSource())
                .actionType(entity.getActionType())
                .abnormalCount(DutyLogDetailEnricher.resolveAbnormalCount(entity))
                .newAbnormalCount(entity.getNewAbnormalCount())
                .recoveredCount(entity.getRecoveredCount())
                .closureSummary(DutyLogDetailEnricher.resolveClosureSummary(entity, null))
                .build();
    }

    private List<DutyLogTimelineVO> buildTimeline(List<DutyLogItemEntity> items) {
        List<DutyLogTimelineVO> timeline = new ArrayList<>();
        for (DutyLogItemEntity item : items) {
            timeline.add(DutyLogTimelineVO.builder()
                    .targetType(item.getTargetType())
                    .targetKey(item.getTargetKey())
                    .targetName(item.getTargetName())
                    .source("item")
                    .changeType(item.getChangeType())
                    .description(DutyLogClosureFormatter.formatItemTimeline(item))
                    .status(item.getStatus())
                    .previousStatus(item.getPreviousStatus())
                    .statusLabel(item.getStatusLabel())
                    .build());
        }
        return timeline;
    }

    private DutyLogItemVO toItemVO(DutyLogItemEntity entity) {
        return DutyLogItemVO.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .targetKey(entity.getTargetKey())
                .targetName(entity.getTargetName())
                .category(entity.getCategory())
                .checkType(entity.getCheckType())
                .status(entity.getStatus())
                .previousStatus(entity.getPreviousStatus())
                .statusLabel(entity.getStatusLabel())
                .changeType(entity.getChangeType())
                .stateToken(entity.getStateToken())
                .detailJson(entity.getDetailJson())
                .build();
    }

    private DutyLogEntity toEntity(DutyLogDTO dto) {
        return DutyLogEntity.builder()
                .userName(dto.getUserName())
                .dutyTime(dto.getDutyTime())
                .siteStatus(dto.getSiteStatus())
                .moduleStatus(dto.getModuleStatus())
                .problem(dto.getProblem())
                .solution(dto.getSolution())
                .recoverTime(dto.getRecoverTime())
                .build();
    }
}
