package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.constant.ModuleCategoryConst;
import com.oceanduty.module.monitor.domain.DashboardVO;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import com.oceanduty.module.monitor.domain.MonitorSiteEntity;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
import com.oceanduty.util.HttpProbeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 监控查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorQueryService {

    private final MonitorSiteDao monitorSiteDao;
    private final MonitorModuleDao monitorModuleDao;
    private final MonitorModuleCheckService monitorModuleCheckService;
    private final MonitorDashboardCacheService monitorDashboardCacheService;

    /**
     * 获取监控仪表盘数据
     */
    public DashboardVO getDashboard() {
        Optional<DashboardVO> cached = monitorDashboardCacheService.get();
        if (cached.isPresent()) {
            return cached.get();
        }
        return loadAndCacheDashboard();
    }

    /**
     * 重新加载并刷新仪表盘缓存
     */
    public DashboardVO loadAndCacheDashboard() {
        monitorModuleCheckService.checkCmsModules();
        return refreshDashboardCache();
    }

    /**
     * 基于当前数据库结果刷新缓存（不再重复检测）
     */
    public DashboardVO refreshDashboardCache() {
        DashboardVO dashboard = buildDashboard();
        monitorDashboardCacheService.put(dashboard);
        return dashboard;
    }

    /**
     * 清除仪表盘缓存
     */
    public void evictDashboardCache() {
        monitorDashboardCacheService.evict();
    }

    private DashboardVO buildDashboard() {
        List<MonitorSiteVO> sites = listAllSites();
        List<MonitorSiteVO> abnormalSites = sites.stream()
                .filter(site -> MonitorStatusConst.ERROR.equals(site.getStatus()))
                .collect(Collectors.toList());
        return DashboardVO.builder()
                .sites(sites)
                .abnormalSites(abnormalSites)
                .modules(listAllModules())
                .build();
    }

    /**
     * 查询全部网站监控数据
     */
    public List<MonitorSiteVO> listAllSites() {
        return monitorSiteDao.selectList(new LambdaQueryWrapper<MonitorSiteEntity>()
                        .orderByAsc(MonitorSiteEntity::getId))
                .stream()
                .map(this::toSiteVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询全部模块监控数据
     */
    public List<MonitorModuleVO> listAllModules() {
        return monitorModuleDao.selectList(new LambdaQueryWrapper<MonitorModuleEntity>()
                        .orderByAsc(MonitorModuleEntity::getId))
                .stream()
                .map(this::toModuleVO)
                .collect(Collectors.toList());
    }

    private MonitorSiteVO toSiteVO(MonitorSiteEntity entity) {
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
                .timeoutMs(entity.getTimeoutMs() == null ? 10000 : entity.getTimeoutMs())
                .responseThreshold(entity.getResponseThreshold() == null ? 3000 : entity.getResponseThreshold())
                .build();
    }

    private MonitorModuleVO toModuleVO(MonitorModuleEntity entity) {
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
