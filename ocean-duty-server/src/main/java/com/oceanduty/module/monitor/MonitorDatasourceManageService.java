package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.module.monitor.domain.MonitorDatasourceDTO;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.module.monitor.domain.MonitorDatasourceQueryDTO;
import com.oceanduty.module.monitor.domain.MonitorDatasourceVO;
import com.oceanduty.third.mysql.CmsForecastAlarmQueryClient;
import com.oceanduty.util.CredentialEncryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模块数据源管理服务
 */
@Service
@RequiredArgsConstructor
public class MonitorDatasourceManageService {

    private static final String PASSWORD_MASK = "******";

    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CmsForecastAlarmQueryClient cmsForecastAlarmQueryClient;
    private final CredentialEncryptUtil credentialEncryptUtil;

    public ResponseDTO<PageResultVO<MonitorDatasourceVO>> queryDatasource(MonitorDatasourceQueryDTO queryDTO) {
        LambdaQueryWrapper<MonitorDatasourceEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getDsName())) {
            wrapper.like(MonitorDatasourceEntity::getDsName, queryDTO.getDsName());
        }
        wrapper.orderByAsc(MonitorDatasourceEntity::getId);

        Page<MonitorDatasourceEntity> page = monitorDatasourceDao.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<MonitorDatasourceVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return ResponseDTO.succ(PageResultVO.<MonitorDatasourceVO>builder()
                .total(page.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
                .list(list)
                .build());
    }

    public ResponseDTO<List<MonitorDatasourceVO>> listAllDatasource() {
        List<MonitorDatasourceVO> list = monitorDatasourceDao.selectList(new LambdaQueryWrapper<MonitorDatasourceEntity>()
                        .eq(MonitorDatasourceEntity::getStatus, 1)
                        .orderByAsc(MonitorDatasourceEntity::getId))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return ResponseDTO.succ(list);
    }

    public ResponseDTO<MonitorDatasourceVO> getDatasource(Long id) {
        MonitorDatasourceEntity entity = monitorDatasourceDao.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        return ResponseDTO.succ(toVO(entity));
    }

    public ResponseDTO<String> addDatasource(MonitorDatasourceDTO dto) {
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "密码不能为空");
        }
        MonitorDatasourceEntity entity = toEntity(dto);
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        monitorDatasourceDao.insert(entity);
        return ResponseDTO.succ();
    }

    public ResponseDTO<String> updateDatasource(MonitorDatasourceDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "数据源ID不能为空");
        }
        MonitorDatasourceEntity exist = monitorDatasourceDao.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        MonitorDatasourceEntity entity = toEntity(dto);
        entity.setId(dto.getId());
        entity.setStatus(dto.getStatus() == null ? exist.getStatus() : dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) {
            entity.setPassword(credentialEncryptUtil.encrypt(dto.getPassword()));
        } else {
            entity.setPassword(exist.getPassword());
        }
        monitorDatasourceDao.updateById(entity);
        return ResponseDTO.succ();
    }

    public ResponseDTO<String> deleteDatasource(Long id) {
        MonitorDatasourceEntity exist = monitorDatasourceDao.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        monitorDatasourceDao.deleteById(id);
        return ResponseDTO.succ();
    }

    public ResponseDTO<String> testDatasource(Long id) {
        MonitorDatasourceEntity entity = monitorDatasourceDao.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        boolean ok = cmsForecastAlarmQueryClient.testConnection(entity);
        if (!ok) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "数据源连接失败");
        }
        return ResponseDTO.succ("连接成功");
    }

    private MonitorDatasourceVO toVO(MonitorDatasourceEntity entity) {
        return MonitorDatasourceVO.builder()
                .id(entity.getId())
                .dsName(entity.getDsName())
                .dsType(entity.getDsType())
                .host(entity.getHost())
                .port(entity.getPort())
                .databaseName(entity.getDatabaseName())
                .username(entity.getUsername())
                .passwordMask(PASSWORD_MASK)
                .tableName(entity.getTableName())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

    private MonitorDatasourceEntity toEntity(MonitorDatasourceDTO dto) {
        return MonitorDatasourceEntity.builder()
                .dsName(dto.getDsName())
                .dsType(dto.getDsType())
                .host(dto.getHost())
                .port(dto.getPort())
                .databaseName(dto.getDatabaseName())
                .username(dto.getUsername())
                .password(credentialEncryptUtil.encrypt(dto.getPassword()))
                .tableName(dto.getTableName())
                .build();
    }
}
