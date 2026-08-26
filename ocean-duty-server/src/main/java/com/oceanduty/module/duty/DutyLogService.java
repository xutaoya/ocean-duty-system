package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.module.duty.domain.DutyLogDTO;
import com.oceanduty.module.duty.domain.DutyLogEntity;
import com.oceanduty.module.duty.domain.DutyLogQueryDTO;
import com.oceanduty.common.domain.RequestUser;
import com.oceanduty.module.duty.domain.DutyLogVO;
import com.oceanduty.util.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班日志服务
 */
@Service
@RequiredArgsConstructor
public class DutyLogService {

    private final DutyLogDao dutyLogDao;

    /**
     * 分页查询值班日志
     */
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

    /**
     * 新增值班日志
     */
    public ResponseDTO<String> addDutyLog(DutyLogDTO dutyLogDTO) {
        DutyLogEntity entity = toEntity(dutyLogDTO);
        entity.setUserName(resolveUserName(dutyLogDTO.getUserName()));
        dutyLogDao.insert(entity);
        return ResponseDTO.succ();
    }

    /**
     * 更新值班日志
     */
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
        dutyLogDao.updateById(entity);
        return ResponseDTO.succ();
    }

    /**
     * 删除值班日志
     */
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
                .siteStatus(entity.getSiteStatus())
                .moduleStatus(entity.getModuleStatus())
                .problem(entity.getProblem())
                .solution(entity.getSolution())
                .recoverTime(entity.getRecoverTime())
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
