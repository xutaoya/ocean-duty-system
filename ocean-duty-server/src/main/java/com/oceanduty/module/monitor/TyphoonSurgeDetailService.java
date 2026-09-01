package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.module.monitor.domain.TyphoonSurgeDetailVO;
import com.oceanduty.module.monitor.domain.TyphoonSurgeMysqlRecord;
import com.oceanduty.third.mysql.CmsForecastAlarmQueryClient;
import com.oceanduty.third.storage.TyphoonSurgeStorageScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 台风风暴潮四步数据链路详情
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TyphoonSurgeDetailService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MonitorModuleDao monitorModuleDao;
    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CmsForecastAlarmQueryClient cmsForecastAlarmQueryClient;
    private final TyphoonSurgeStorageScanner typhoonSurgeStorageScanner;

    public TyphoonSurgeDetailVO getDetailByModuleId(Long moduleId) {
        MonitorModuleEntity module = monitorModuleDao.selectById(moduleId);
        if (module == null || !ModuleCheckTypeConst.TYPHOON_STORM_SURGE_CHAIN.equals(module.getCheckType())) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND, "模块不存在或非台风风暴潮类型");
        }
        Map<String, String> params = parseParams(module.getCheckParam());
        Long mysqlDatasourceId = parseLong(params.get("mysqlDatasourceId"));
        Long pgDatasourceId = parseLong(params.get("pgDatasourceId"));
        if (mysqlDatasourceId == null || pgDatasourceId == null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "模块检测参数不完整");
        }
        MonitorDatasourceEntity mysqlDatasource = monitorDatasourceDao.selectById(mysqlDatasourceId);
        MonitorDatasourceEntity pgDatasource = monitorDatasourceDao.selectById(pgDatasourceId);
        return buildDetail(mysqlDatasource, pgDatasource);
    }

    private TyphoonSurgeDetailVO buildDetail(MonitorDatasourceEntity mysqlDatasource,
                                             MonitorDatasourceEntity pgDatasource) {
        List<String> remarks = new ArrayList<>();
        TyphoonSurgeDetailVO.TyphoonSurgeDetailVOBuilder builder = TyphoonSurgeDetailVO.builder();

        TyphoonSurgeMysqlRecord mysqlRecord = cmsForecastAlarmQueryClient.fetchTyphoonSurgeMysqlLatest(mysqlDatasource);
        if (mysqlRecord != null) {
            builder.initialTime(mysqlRecord.getInitialTime())
                    .updateTime(mysqlRecord.getUpdateTime());
        } else if (mysqlDatasource == null) {
            remarks.add("数据库: 未配置数据源");
        }

        LocalDateTime pgDoneStamp = cmsForecastAlarmQueryClient.fetchLatestDoneStamp(pgDatasource);
        builder.pgDoneStamp(pgDoneStamp);
        if (pgDoneStamp == null && pgDatasource != null) {
            remarks.add("处理后: 连接失败或暂无数据");
        } else if (pgDatasource == null) {
            remarks.add("处理后: 未配置数据源");
        }

        TyphoonSurgeStorageScanner.FileScanResult ftpResult = typhoonSurgeStorageScanner.scanFtp();
        if (ftpResult != null) {
            if (StringUtils.hasText(ftpResult.errorMessage())) {
                remarks.add("处理前: " + ftpResult.errorMessage());
            } else if (ftpResult.modifiedTime() != null) {
                builder.ftpFileName(ftpResult.fileName())
                        .ftpModifiedTime(ftpResult.modifiedTime())
                        .ftpFileSizeBytes(ftpResult.fileSizeBytes());
            }
        }

        TyphoonSurgeStorageScanner.FileScanResult rawResult = typhoonSurgeStorageScanner.scanRawShare();
        if (rawResult != null) {
            if (StringUtils.hasText(rawResult.errorMessage())) {
                remarks.add("原始文件: " + rawResult.errorMessage());
            } else if (rawResult.modifiedTime() != null) {
                builder.rawFolder(rawResult.folder())
                        .rawFileName(rawResult.fileName())
                        .rawModifiedTime(rawResult.modifiedTime())
                        .rawFileSizeBytes(rawResult.fileSizeBytes());
            }
        }

        if (!remarks.isEmpty()) {
            builder.remark(String.join("；", remarks));
        }
        appendMissingRemark(builder);
        return builder.build();
    }

    private void appendMissingRemark(TyphoonSurgeDetailVO.TyphoonSurgeDetailVOBuilder builder) {
        TyphoonSurgeDetailVO draft = builder.build();
        if (StringUtils.hasText(draft.getRemark())) {
            return;
        }
        List<String> missing = new ArrayList<>();
        if (draft.getInitialTime() == null || draft.getUpdateTime() == null) {
            missing.add("数据库");
        }
        if (draft.getPgDoneStamp() == null) {
            missing.add("处理后");
        }
        if (draft.getFtpModifiedTime() == null) {
            missing.add("处理前");
        }
        if (draft.getRawModifiedTime() == null) {
            missing.add("原始文件");
        }
        if (!missing.isEmpty()) {
            builder.remark("未获取: " + String.join("、", missing));
        }
    }

    private Map<String, String> parseParams(String checkParam) {
        if (!StringUtils.hasText(checkParam)) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(checkParam, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("台风风暴潮参数解析失败: {}", checkParam);
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
