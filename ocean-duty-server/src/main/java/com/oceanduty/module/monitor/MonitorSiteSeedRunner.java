package com.oceanduty.module.monitor;

import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.MonitorSiteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监控网站种子数据同步
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class MonitorSiteSeedRunner implements CommandLineRunner {

    private final MonitorSiteDao monitorSiteDao;

    @Override
    public void run(String... args) {
        List<MonitorSiteEntity> sites = buildSites();
        for (MonitorSiteEntity site : sites) {
            MonitorSiteEntity exist = monitorSiteDao.selectById(site.getId());
            if (exist == null) {
                monitorSiteDao.insert(site);
            }
        }
        log.info("监控网站初始数据检查完成，默认配置 {} 项", sites.size());
    }

    private List<MonitorSiteEntity> buildSites() {
        return List.of(
                site(1L, "中国海洋预报网", "https://www.oceanguide.org.cn/IndexHome", "portal"),
                site(2L, "国家海洋预报中心门户网站", "https://www.nmefc.cn/", "portal"),
                site(3L, "海洋灾害子场景", "https://www.nmefc.cn/", "subscene"),
                site(4L, "NEARGOOS网站", "https://neargoos.nmefc.cn/#/index", "portal"),
                site(5L, "MaCOM网站", "https://macom.oceanguide.org.cn/", "portal")
        );
    }

    private MonitorSiteEntity site(Long id, String name, String url, String type) {
        return MonitorSiteEntity.builder()
                .id(id)
                .siteName(name)
                .siteUrl(url)
                .siteType(type)
                .status(MonitorStatusConst.NORMAL)
                .timeoutMs(10000)
                .responseThreshold(3000)
                .build();
    }
}
