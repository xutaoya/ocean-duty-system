package com.oceanduty.module.monitor;

import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.MonitorSiteEntity;
import com.oceanduty.util.HttpProbeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 网站监控检测服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorCheckService {

    private final MonitorSiteDao monitorSiteDao;

    /**
     * 执行全部网站访问检测
     */
    public void checkAllSites() {
        List<MonitorSiteEntity> sites = monitorSiteDao.selectList(null);
        for (MonitorSiteEntity site : sites) {
            checkSite(site);
        }
    }

    /**
     * 检测单个网站
     */
    public void checkSite(MonitorSiteEntity site) {
        HttpProbeUtil.ProbeResult result = HttpProbeUtil.probe(site.getSiteUrl());
        site.setResponseTime(result.responseTime());
        site.setLastCheckTime(LocalDateTime.now());

        if (result.errorMessage() != null) {
            site.setStatus(MonitorStatusConst.ERROR);
            site.setHttpStatus(null);
            site.setErrorMessage(result.errorMessage());
        } else if (result.httpStatus() != null && result.httpStatus() >= 200 && result.httpStatus() < 400) {
            site.setStatus(MonitorStatusConst.NORMAL);
            site.setHttpStatus(result.httpStatus());
            site.setErrorMessage(null);
        } else {
            site.setStatus(MonitorStatusConst.ERROR);
            site.setHttpStatus(result.httpStatus());
            site.setErrorMessage("HTTP状态码异常: " + result.httpStatus());
        }

        monitorSiteDao.updateById(site);
        log.info("网站检测完成: {} -> status={}", site.getSiteName(), site.getStatus());
    }
}
