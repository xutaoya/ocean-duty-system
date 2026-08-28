package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.config.SmartGridVersionProperties;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.module.monitor.domain.SmartGridDetailVO;
import com.oceanduty.third.ftp.SmartGridFtpScanner;
import com.oceanduty.third.mysql.CmsForecastAlarmQueryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 智能网格扩展信息聚合（起报时间 + FTP 文件时间）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartGridDetailService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MonitorModuleDao monitorModuleDao;
    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CmsForecastAlarmQueryClient cmsForecastAlarmQueryClient;
    private final SmartGridFtpScanner smartGridFtpScanner;
    private final SmartGridVersionProperties versionProperties;

    /**
     * 按模块 ID 加载单个智能网格要素扩展信息
     */
    public SmartGridDetailVO getDetailByModuleId(Long moduleId) {
        MonitorModuleEntity module = monitorModuleDao.selectById(moduleId);
        if (module == null || !ModuleCheckTypeConst.CMS_GRID_UPDATE.equals(module.getCheckType())) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND, "模块不存在或非智能网格类型");
        }
        String elementKey = parseElementKey(module.getCheckParam());
        if (!StringUtils.hasText(elementKey)) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "模块检测参数不完整");
        }
        SmartGridElementCatalog.ElementDef element = SmartGridElementCatalog.ELEMENTS.stream()
                .filter(item -> item.key().equals(elementKey))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResponseCodeConst.NOT_FOUND, "未找到智能网格要素配置"));
        SmartGridDetailVO detail = loadDetail(element);
        detail.setLinkedModuleIds(findLinkedModuleIds(moduleId, element));
        detail.setShowOutput(element.scanOutput());
        return detail;
    }

    /**
     * 按模块 ID 加载单个智能网格要素扩展信息
     */
    public Optional<SmartGridDetailVO> loadDetailByModuleId(Long moduleId) {
        try {
            return Optional.of(getDetailByModuleId(moduleId));
        } catch (BusinessException e) {
            return Optional.empty();
        }
    }

    private SmartGridDetailVO loadDetail(SmartGridElementCatalog.ElementDef element) {
        MonitorDatasourceEntity mysqlDatasource = monitorDatasourceDao.selectById(versionProperties.getMysqlDatasourceId());
        MonitorDatasourceEntity pgDatasource = monitorDatasourceDao.selectById(element.pgDatasourceId());
        SmartGridFtpScanner.FtpScanResult ftpResult = smartGridFtpScanner.scan(element);
        return buildDetail(element, mysqlDatasource, pgDatasource, ftpResult);
    }

    private SmartGridDetailVO buildDetail(SmartGridElementCatalog.ElementDef element,
                                          MonitorDatasourceEntity mysqlDatasource,
                                          MonitorDatasourceEntity pgDatasource,
                                          SmartGridFtpScanner.FtpScanResult ftpResult) {
        SmartGridDetailVO.SmartGridDetailVOBuilder builder = SmartGridDetailVO.builder()
                .elementKey(element.key())
                .elementName(element.name());

        String version = fetchPgVersion(pgDatasource);
        builder.pgVersion(version);
        builder.reportStartTime(fetchReportStartTime(mysqlDatasource, version));

        SmartGridFtpScanner.FtpScanResult scanResult = ftpResult == null
                ? new SmartGridFtpScanner.FtpScanResult(null, null, "FTP 扫描结果为空")
                : ftpResult;
        if (StringUtils.hasText(scanResult.errorMessage())) {
            builder.remark(scanResult.errorMessage());
        }
        if (scanResult.output() != null) {
            builder.outputFileName(scanResult.output().fileName())
                    .outputDataTime(scanResult.output().dataTime())
                    .outputModifiedTime(scanResult.output().modifiedTime())
                    .outputFileSizeBytes(scanResult.output().fileSizeBytes());
        }
        if (scanResult.element() != null) {
            LocalDateTime elementDataTime = scanResult.element().dataTime();
            int offsetHours = SmartGridElementCatalog.elementDataTimeOffsetHours(element.key());
            if (elementDataTime != null && offsetHours > 0) {
                elementDataTime = elementDataTime.plusHours(offsetHours);
            }
            builder.elementFolder(scanResult.element().folder())
                    .elementFileName(scanResult.element().fileName())
                    .elementDataTime(elementDataTime)
                    .elementModifiedTime(scanResult.element().modifiedTime())
                    .elementFileSizeBytes(scanResult.element().fileSizeBytes());
        }
        appendMissingRemark(builder, element);
        return builder.build();
    }

    private List<Long> findLinkedModuleIds(Long moduleId, SmartGridElementCatalog.ElementDef element) {
        if (!StringUtils.hasText(element.detailGroup())) {
            return List.of(moduleId);
        }
        return monitorModuleDao.selectList(new LambdaQueryWrapper<MonitorModuleEntity>()
                        .eq(MonitorModuleEntity::getCheckType, ModuleCheckTypeConst.CMS_GRID_UPDATE))
                .stream()
                .filter(item -> element.detailGroup().equals(
                        SmartGridElementCatalog.resolveDetailGroup(parseElementKey(item.getCheckParam()))))
                .map(MonitorModuleEntity::getId)
                .collect(Collectors.toList());
    }

    private String fetchPgVersion(MonitorDatasourceEntity pgDatasource) {
        if (pgDatasource == null) {
            return null;
        }
        return cmsForecastAlarmQueryClient.fetchLatestVersion(pgDatasource, pgDatasource.getTableName(), "version");
    }

    private LocalDateTime fetchReportStartTime(MonitorDatasourceEntity mysqlDatasource, String version) {
        if (mysqlDatasource == null || !StringUtils.hasText(version)) {
            return null;
        }
        return cmsForecastAlarmQueryClient.fetchReportDate(
                mysqlDatasource,
                versionProperties.getControllerTable(),
                version);
    }

    private void appendMissingRemark(SmartGridDetailVO.SmartGridDetailVOBuilder builder,
                                     SmartGridElementCatalog.ElementDef element) {
        SmartGridDetailVO draft = builder.build();
        if (StringUtils.hasText(draft.getRemark())) {
            return;
        }
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(draft.getPgVersion())) {
            missing.add("PG版本");
        }
        if (draft.getReportStartTime() == null) {
            missing.add("起报时间");
        }
        if (element.scanOutput() && draft.getOutputDataTime() == null) {
            missing.add("Output数据");
        }
        if (draft.getElementDataTime() == null) {
            missing.add("要素数据");
        }
        if (!missing.isEmpty()) {
            builder.remark("未获取: " + String.join("、", missing));
        }
    }

    private String parseElementKey(String checkParam) {
        if (!StringUtils.hasText(checkParam)) {
            return null;
        }
        try {
            Map<String, String> params = OBJECT_MAPPER.readValue(checkParam, new TypeReference<>() {
            });
            return params.get("windowPreset");
        } catch (Exception e) {
            log.warn("智能网格参数解析失败: {}", checkParam);
            return null;
        }
    }
}
