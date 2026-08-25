package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.module.monitor.domain.MonitorSiteDTO;
import com.oceanduty.module.monitor.domain.MonitorSiteEntity;
import com.oceanduty.module.monitor.domain.MonitorSiteQueryDTO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 网站管理服务
 */
@Service
@RequiredArgsConstructor
public class MonitorSiteManageService {

    private static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int DEFAULT_RESPONSE_THRESHOLD = 3000;

    private final MonitorSiteDao monitorSiteDao;

    /**
     * 分页查询网站
     */
    public ResponseDTO<PageResultVO<MonitorSiteVO>> querySite(MonitorSiteQueryDTO queryDTO) {
        LambdaQueryWrapper<MonitorSiteEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getSiteName())) {
            wrapper.like(MonitorSiteEntity::getSiteName, queryDTO.getSiteName());
        }
        if (StringUtils.hasText(queryDTO.getSiteType())) {
            wrapper.eq(MonitorSiteEntity::getSiteType, queryDTO.getSiteType());
        }
        wrapper.orderByAsc(MonitorSiteEntity::getId);

        Page<MonitorSiteEntity> page = monitorSiteDao.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<MonitorSiteVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return ResponseDTO.succ(PageResultVO.<MonitorSiteVO>builder()
                .total(page.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
                .list(list)
                .build());
    }

    /**
     * 查询网站详情
     */
    public ResponseDTO<MonitorSiteVO> getSite(Long id) {
        MonitorSiteEntity entity = monitorSiteDao.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        return ResponseDTO.succ(toVO(entity));
    }

    /**
     * 新增网站
     */
    public ResponseDTO<String> addSite(MonitorSiteDTO siteDTO) {
        MonitorSiteEntity entity = toEntity(siteDTO);
        monitorSiteDao.insert(entity);
        return ResponseDTO.succ();
    }

    /**
     * 更新网站
     */
    public ResponseDTO<String> updateSite(MonitorSiteDTO siteDTO) {
        if (siteDTO.getId() == null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "网站ID不能为空");
        }
        MonitorSiteEntity exist = monitorSiteDao.selectById(siteDTO.getId());
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        MonitorSiteEntity entity = toEntity(siteDTO);
        entity.setId(siteDTO.getId());
        entity.setStatus(exist.getStatus());
        entity.setHttpStatus(exist.getHttpStatus());
        entity.setResponseTime(exist.getResponseTime());
        entity.setLastCheckTime(exist.getLastCheckTime());
        entity.setErrorMessage(exist.getErrorMessage());
        monitorSiteDao.updateById(entity);
        return ResponseDTO.succ();
    }

    /**
     * 删除网站
     */
    public ResponseDTO<String> deleteSite(Long id) {
        MonitorSiteEntity exist = monitorSiteDao.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        monitorSiteDao.deleteById(id);
        return ResponseDTO.succ();
    }

    private MonitorSiteVO toVO(MonitorSiteEntity entity) {
        return MonitorSiteVO.builder()
                .id(entity.getId())
                .siteName(entity.getSiteName())
                .siteUrl(entity.getSiteUrl())
                .siteType(entity.getSiteType())
                .status(entity.getStatus())
                .httpStatus(entity.getHttpStatus())
                .responseTime(entity.getResponseTime())
                .lastCheckTime(entity.getLastCheckTime())
                .errorMessage(entity.getErrorMessage())
                .timeoutMs(resolveTimeout(entity.getTimeoutMs()))
                .responseThreshold(resolveThreshold(entity.getResponseThreshold()))
                .build();
    }

    private MonitorSiteEntity toEntity(MonitorSiteDTO dto) {
        return MonitorSiteEntity.builder()
                .siteName(dto.getSiteName())
                .siteUrl(dto.getSiteUrl())
                .siteType(dto.getSiteType())
                .timeoutMs(dto.getTimeoutMs())
                .responseThreshold(dto.getResponseThreshold())
                .build();
    }

    private Integer resolveTimeout(Integer timeoutMs) {
        return timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }

    private Integer resolveThreshold(Integer threshold) {
        return threshold == null ? DEFAULT_RESPONSE_THRESHOLD : threshold;
    }
}
