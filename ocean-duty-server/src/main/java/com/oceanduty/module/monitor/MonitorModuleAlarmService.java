package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.module.monitor.domain.CmsForecastAlarmDetailVO;
import com.oceanduty.module.monitor.domain.CmsForecastAlarmRecord;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.third.mysql.CmsForecastAlarmQueryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 模块 CMS 警报详情服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorModuleAlarmService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MonitorModuleDao monitorModuleDao;
    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CmsForecastAlarmQueryClient cmsForecastAlarmQueryClient;

    /**
     * 查询模块关联的最新 CMS 警报详情
     */
    public ResponseDTO<CmsForecastAlarmDetailVO> getAlarmDetail(Long moduleId) {
        MonitorModuleEntity module = monitorModuleDao.selectById(moduleId);
        if (module == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        if (!ModuleCheckTypeConst.CMS_FORECAST_ALARM.equals(module.getCheckType())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "该模块不支持查看 CMS 警报详情");
        }

        Map<String, String> params = parseCheckParam(module.getCheckParam());
        Long datasourceId = parseLong(params.get("datasourceId"));
        String alarmType = params.get("type");
        if (datasourceId == null || !StringUtils.hasText(alarmType)) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "模块检测参数不完整");
        }

        MonitorDatasourceEntity datasource = monitorDatasourceDao.selectById(datasourceId);
        if (datasource == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND, "数据源不存在");
        }

        CmsForecastAlarmRecord record = cmsForecastAlarmQueryClient.fetchLatestDetail(datasource, alarmType);
        if (record == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND, "暂无最新警报数据");
        }

        return ResponseDTO.succ(CmsForecastAlarmDetailVO.builder()
                .moduleId(module.getId())
                .moduleName(module.getModuleName())
                .moduleGroup(module.getModuleGroup())
                .alarmType(alarmType)
                .alarmTypeName(resolveAlarmTypeName(alarmType))
                .title(record.getTitle())
                .code(record.getCode())
                .alarmDate(record.getAlarmDate())
                .level(record.getLevel())
                .image(record.getImage())
                .description(record.getDescription())
                .defenseGuide(record.getDefenseGuide())
                .standard(record.getStandard())
                .content(record.getContent())
                .build());
    }

    private String resolveAlarmTypeName(String alarmType) {
        return switch (alarmType) {
            case "wave" -> "海浪";
            case "storm" -> "风暴潮";
            case "bore" -> "海啸";
            case "ice" -> "海冰";
            default -> alarmType;
        };
    }

    private Map<String, String> parseCheckParam(String checkParam) {
        if (!StringUtils.hasText(checkParam)) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(checkParam, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            log.warn("模块检测参数解析失败: {}", checkParam);
            return Map.of();
        }
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
