package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.ModuleCategoryConst;
import com.oceanduty.module.monitor.domain.MonitorModuleDTO;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.module.monitor.domain.MonitorModuleQueryDTO;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 监控模块管理服务
 */
@Service
@RequiredArgsConstructor
public class MonitorModuleManageService {

    private final MonitorModuleDao monitorModuleDao;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询监控模块
     */
    public ResponseDTO<PageResultVO<MonitorModuleVO>> queryModule(MonitorModuleQueryDTO queryDTO) {
        LambdaQueryWrapper<MonitorModuleEntity> wrapper = buildQueryWrapper(queryDTO);
        Page<MonitorModuleEntity> page = monitorModuleDao.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<MonitorModuleVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return ResponseDTO.succ(PageResultVO.<MonitorModuleVO>builder()
                .total(page.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
                .list(list)
                .build());
    }

    /**
     * 查询模块详情
     */
    public ResponseDTO<MonitorModuleVO> getModule(Long id) {
        MonitorModuleEntity entity = monitorModuleDao.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        return ResponseDTO.succ(toVO(entity));
    }

    /**
     * 新增监控模块
     */
    public ResponseDTO<String> addModule(MonitorModuleDTO moduleDTO) {
        validateModule(moduleDTO);
        monitorModuleDao.insert(toEntity(moduleDTO));
        return ResponseDTO.succ();
    }

    /**
     * 更新监控模块
     */
    public ResponseDTO<String> updateModule(MonitorModuleDTO moduleDTO) {
        if (moduleDTO.getId() == null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "模块ID不能为空");
        }
        MonitorModuleEntity exist = monitorModuleDao.selectById(moduleDTO.getId());
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        validateModule(moduleDTO);
        MonitorModuleEntity entity = toEntity(moduleDTO);
        entity.setId(moduleDTO.getId());
        entity.setStatus(exist.getStatus());
        entity.setDataUpdateTime(exist.getDataUpdateTime());
        entity.setLastCheckTime(exist.getLastCheckTime());
        entity.setRemark(exist.getRemark());
        entity.setAlarmTitle(exist.getAlarmTitle());
        entity.setAlarmCode(exist.getAlarmCode());
        entity.setAlarmLevel(exist.getAlarmLevel());
        monitorModuleDao.updateById(entity);
        return ResponseDTO.succ();
    }

    /**
     * 删除监控模块
     */
    public ResponseDTO<String> deleteModule(Long id) {
        MonitorModuleEntity exist = monitorModuleDao.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        monitorModuleDao.deleteById(id);
        return ResponseDTO.succ();
    }

    private LambdaQueryWrapper<MonitorModuleEntity> buildQueryWrapper(MonitorModuleQueryDTO queryDTO) {
        LambdaQueryWrapper<MonitorModuleEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getModuleName())) {
            wrapper.like(MonitorModuleEntity::getModuleName, queryDTO.getModuleName());
        }
        if (StringUtils.hasText(queryDTO.getModuleCategory())) {
            wrapper.eq(MonitorModuleEntity::getModuleCategory, queryDTO.getModuleCategory());
        }
        if (StringUtils.hasText(queryDTO.getModuleGroup())) {
            wrapper.like(MonitorModuleEntity::getModuleGroup, queryDTO.getModuleGroup());
        }
        wrapper.orderByAsc(MonitorModuleEntity::getId);
        return wrapper;
    }

    private void validateModule(MonitorModuleDTO moduleDTO) {
        if (!ModuleCategoryConst.DISASTER_WARNING.equals(moduleDTO.getModuleCategory())
                && !ModuleCategoryConst.FORECAST_SERVICE.equals(moduleDTO.getModuleCategory())
                && !ModuleCategoryConst.ENV_FORECAST.equals(moduleDTO.getModuleCategory())
                && !ModuleCategoryConst.SMART_GRID.equals(moduleDTO.getModuleCategory())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "模块分类不正确");
        }
        if (!moduleDTO.getExpectedTime().matches("\\d{2}:\\d{2}")) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "预期更新时间格式应为HH:mm");
        }
        try {
            objectMapper.readTree(moduleDTO.getCheckParam());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "检测参数必须是合法JSON");
        }
    }

    private MonitorModuleVO toVO(MonitorModuleEntity entity) {
        return MonitorModuleVO.builder()
                .id(entity.getId())
                .siteId(entity.getSiteId())
                .moduleName(entity.getModuleName())
                .moduleUrl(entity.getModuleUrl())
                .moduleCategory(entity.getModuleCategory())
                .moduleCategoryName(resolveCategoryName(entity.getModuleCategory()))
                .moduleGroup(entity.getModuleGroup())
                .checkType(entity.getCheckType())
                .checkParam(entity.getCheckParam())
                .updateTime(entity.getDataUpdateTime())
                .expectedTime(entity.getExpectedTime())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .alarmTitle(entity.getAlarmTitle())
                .alarmCode(entity.getAlarmCode())
                .alarmLevel(entity.getAlarmLevel())
                .lastCheckTime(entity.getLastCheckTime())
                .build();
    }

    private MonitorModuleEntity toEntity(MonitorModuleDTO dto) {
        return MonitorModuleEntity.builder()
                .siteId(dto.getSiteId())
                .moduleName(dto.getModuleName())
                .moduleUrl(dto.getModuleUrl())
                .moduleCategory(dto.getModuleCategory())
                .moduleGroup(dto.getModuleGroup())
                .checkType(dto.getCheckType())
                .checkParam(dto.getCheckParam())
                .expectedTime(dto.getExpectedTime())
                .build();
    }

    private String resolveCategoryName(String category) {
        if (ModuleCategoryConst.FORECAST_SERVICE.equals(category)) {
            return "预报服务";
        }
        if (ModuleCategoryConst.ENV_FORECAST.equals(category)) {
            return "环境预报";
        }
        if (ModuleCategoryConst.SMART_GRID.equals(category)) {
            return "智能网格";
        }
        return "灾害预警";
    }
}
