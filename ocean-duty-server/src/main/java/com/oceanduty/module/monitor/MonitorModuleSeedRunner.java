package com.oceanduty.module.monitor;

import com.oceanduty.constant.ModuleCategoryConst;
import com.oceanduty.constant.ModuleCheckTypeConst;
import com.oceanduty.constant.MonitorStatusConst;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监控模块种子数据同步
 */
@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class MonitorModuleSeedRunner implements CommandLineRunner {

    private final MonitorModuleDao monitorModuleDao;

    @Override
    public void run(String... args) {
        List<MonitorModuleEntity> modules = buildModules();
        for (MonitorModuleEntity module : modules) {
            MonitorModuleEntity exist = monitorModuleDao.selectById(module.getId());
            if (exist == null) {
                monitorModuleDao.insert(module);
            }
        }
        log.info("监控模块初始数据检查完成，默认配置 {} 项", modules.size());
    }

    private List<MonitorModuleEntity> buildModules() {
        return List.of(
                cmsModule(24L, 1L, "海浪警报", "https://www.oceanguide.org.cn/IndexHome",
                        "wave", "08:00"),
                cmsModule(25L, 1L, "风暴潮警报", "https://www.oceanguide.org.cn/IndexHome",
                        "storm", "08:00"),
                cmsModule(26L, 1L, "海啸警报", "https://www.oceanguide.org.cn/IndexHome",
                        "bore", "08:00"),
                cmsModule(27L, 1L, "海冰警报", "https://www.oceanguide.org.cn/IndexHome",
                        "ice", "08:00"),

                envModule(28L, 1L, "海区预报", "https://www.oceanguide.org.cn/IndexHome",
                        2L, "name", "15:30"),
                envModule(29L, 1L, "近岸预报", "https://www.oceanguide.org.cn/IndexHome",
                        3L, "code", "09:00"),
                envMonthlyModule(30L, 1L, "月预报", "https://www.oceanguide.org.cn/IndexHome",
                        4L, "1190087852779372544", "00:00"),

                gridModule(31L, 1L, "风", 5L, "wind"),
                gridModule(32L, 1L, "海浪", 6L, "wave"),
                gridModule(33L, 1L, "海流", 7L, "current"),
                gridModule(34L, 1L, "海温", 8L, "sst"),
                gridModule(35L, 1L, "天文潮", 9L, "storm_tide")
        );
    }

    private MonitorModuleEntity gridModule(Long id, Long siteId, String name, Long datasourceId, String windowPreset) {
        return module(id, siteId, name, "https://www.oceanguide.org.cn/IndexHome",
                ModuleCategoryConst.SMART_GRID, "中国海洋预报网--智能网格",
                ModuleCheckTypeConst.CMS_GRID_UPDATE,
                "{\"datasourceId\":\"" + datasourceId + "\",\"timeField\":\"update_date\",\"windowPreset\":\""
                        + windowPreset + "\"}",
                "00:00");
    }

    private MonitorModuleEntity envModule(Long id, Long siteId, String name, String url,
                                          Long datasourceId, String titleField, String expectedTime) {
        return module(id, siteId, name, url,
                ModuleCategoryConst.ENV_FORECAST, "中国海洋预报网--环境预报",
                ModuleCheckTypeConst.CMS_TABLE_PUBLISH,
                "{\"datasourceId\":\"" + datasourceId + "\",\"timeField\":\"create_date\","
                        + "\"titleField\":\"" + titleField + "\",\"scheduleType\":\"daily\"}",
                expectedTime);
    }

    private MonitorModuleEntity envMonthlyModule(Long id, Long siteId, String name, String url,
                                                 Long datasourceId, String categoryId, String expectedTime) {
        return module(id, siteId, name, url,
                ModuleCategoryConst.ENV_FORECAST, "中国海洋预报网--环境预报",
                ModuleCheckTypeConst.CMS_TABLE_PUBLISH,
                "{\"datasourceId\":\"" + datasourceId + "\",\"timeField\":\"create_date\","
                        + "\"titleField\":\"title\",\"scheduleType\":\"monthly\",\"categoryId\":\""
                        + categoryId + "\"}",
                expectedTime);
    }

    private MonitorModuleEntity cmsModule(Long id, Long siteId, String name, String url,
                                          String alarmType, String expectedTime) {
        return module(id, siteId, name, url,
                ModuleCategoryConst.DISASTER_WARNING, "中国海洋预报网",
                ModuleCheckTypeConst.CMS_FORECAST_ALARM,
                "{\"datasourceId\":\"1\",\"type\":\"" + alarmType + "\"}",
                expectedTime);
    }

    private MonitorModuleEntity module(Long id, Long siteId, String name, String url, String category,
                                       String group, String checkType, String checkParam, String expectedTime) {
        return MonitorModuleEntity.builder()
                .id(id)
                .siteId(siteId)
                .moduleName(name)
                .moduleUrl(url)
                .moduleCategory(category)
                .moduleGroup(group)
                .checkType(checkType)
                .checkParam(checkParam)
                .expectedTime(expectedTime)
                .status(MonitorStatusConst.NORMAL)
                .build();
    }
}
