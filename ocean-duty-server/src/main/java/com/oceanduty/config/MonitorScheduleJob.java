package com.oceanduty.config;

import com.oceanduty.module.monitor.MonitorCheckService;
import com.oceanduty.module.monitor.MonitorModuleCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 监控定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorScheduleJob {

    private final MonitorCheckService monitorCheckService;
    private final MonitorModuleCheckService monitorModuleCheckService;

    /**
     * 每5分钟检测网站访问
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkSites() {
        log.info("开始执行网站监控定时任务");
        monitorCheckService.checkAllSites();
    }

    /**
     * 每10分钟检测模块更新时间
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void checkModules() {
        log.info("开始执行模块监控定时任务");
        monitorModuleCheckService.checkAllModules();
    }
}
