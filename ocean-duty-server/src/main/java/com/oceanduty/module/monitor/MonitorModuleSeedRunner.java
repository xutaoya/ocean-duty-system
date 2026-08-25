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
@Order(2)
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
        Long siteId = 2L;
        return List.of(
                module(1L, siteId, "台风海浪警报", "https://www.nmefc.cn/zhyj/hljb/tfhljb",
                        ModuleCategoryConst.DISASTER_WARNING, "海浪警报", ModuleCheckTypeConst.WARN_HISTORY,
                        "{\"warnType\":\"wave\",\"filter\":\"WaveWarning_TaiFeng\"}", "08:00"),
                module(2L, siteId, "温带海浪警报", "https://www.nmefc.cn/zhyj/hljb/wdhljb",
                        ModuleCategoryConst.DISASTER_WARNING, "海浪警报", ModuleCheckTypeConst.WARN_HISTORY,
                        "{\"warnType\":\"wave\",\"filter\":\"WaveWarning_WenDai\"}", "08:00"),
                module(3L, siteId, "台风风暴潮警报", "https://www.nmefc.cn/zhyj/fbcjb/tffbcjb",
                        ModuleCategoryConst.DISASTER_WARNING, "风暴潮警报", ModuleCheckTypeConst.WARN_HISTORY,
                        "{\"warnType\":\"storm\",\"filter\":\"StormSurgeWarning_TaiFeng\"}", "08:00"),
                module(4L, siteId, "温带风暴潮警报", "https://www.nmefc.cn/zhyj/fbcjb/wdfbcjb",
                        ModuleCategoryConst.DISASTER_WARNING, "风暴潮警报", ModuleCheckTypeConst.WARN_HISTORY,
                        "{\"warnType\":\"storm\",\"filter\":\"StormSurgeWarning_WenDai\"}", "08:00"),
                module(5L, siteId, "海冰警报", "https://www.nmefc.cn/zhyj/hbjb",
                        ModuleCategoryConst.DISASTER_WARNING, "海冰警报", ModuleCheckTypeConst.WARN_HISTORY,
                        "{\"warnType\":\"ice\"}", "08:00"),
                module(6L, siteId, "海啸消息/警报", "https://www.nmefc.cn/zhyj/hx",
                        ModuleCategoryConst.DISASTER_WARNING, "海啸消息/警报", ModuleCheckTypeConst.WARN_HISTORY,
                        "{\"warnType\":\"tsunami\"}", "08:00"),

                module(7L, siteId, "海浪实况", "https://www.nmefc.cn/ybfw/waveAnalysis",
                        ModuleCategoryConst.FORECAST_SERVICE, "海浪", ModuleCheckTypeConst.ANALYSIS_LIST,
                        "{\"type\":\"waveAnalysis\"}", "08:00"),
                module(8L, siteId, "海浪综合预报", "https://www.nmefc.cn/ybfw/waveForecast",
                        ModuleCategoryConst.FORECAST_SERVICE, "海浪", ModuleCheckTypeConst.ANALYSIS_LIST,
                        "{\"type\":\"waveForecast\"}", "08:00"),
                module(9L, siteId, "海浪预报", "https://www.nmefc.cn/ybfw/wave/Global",
                        ModuleCategoryConst.FORECAST_SERVICE, "海浪", ModuleCheckTypeConst.NUMERICAL_LIST,
                        "{\"element\":\"wave\",\"regioncode\":\"Global\"}", "08:00"),
                module(10L, siteId, "台风风暴潮预报", "https://www.nmefc.cn/stormSurgeViews/typhoon",
                        ModuleCategoryConst.FORECAST_SERVICE, "风暴潮", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"typhoon\"}", "08:00"),
                module(11L, siteId, "温带风暴潮预报", "https://www.nmefc.cn/stormSurgeViews/temperate",
                        ModuleCategoryConst.FORECAST_SERVICE, "风暴潮", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"temperate\"}", "08:00"),
                module(12L, siteId, "海冰年预报", "https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Year",
                        ModuleCategoryConst.FORECAST_SERVICE, "海冰综合预报", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"IceForecast-Year\"}", "08:00"),
                module(13L, siteId, "海冰月预报", "https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Moth",
                        ModuleCategoryConst.FORECAST_SERVICE, "海冰综合预报", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"IceForecast-Moth\"}", "08:00"),
                module(14L, siteId, "海冰旬预报", "https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Ten",
                        ModuleCategoryConst.FORECAST_SERVICE, "海冰综合预报", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"IceForecast-Ten\"}", "08:00"),
                module(15L, siteId, "海冰周预报", "https://www.nmefc.cn/ybfw/IceForecast/IceForecast-Week",
                        ModuleCategoryConst.FORECAST_SERVICE, "海冰综合预报", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"IceForecast-Week\"}", "08:00"),
                module(16L, siteId, "海冰预报", "https://www.nmefc.cn/ybfw/IceNumerical/BoHuangHaiBusiness",
                        ModuleCategoryConst.FORECAST_SERVICE, "海冰", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"IceNumerical\"}", "08:00"),
                module(17L, siteId, "极地预报", "https://www.nmefc.cn/ybfw/polar/ArcticPolarIce",
                        ModuleCategoryConst.FORECAST_SERVICE, "极地", ModuleCheckTypeConst.POLAR_REGIONS_LIST,
                        "{\"region\":\"ArcticPolarIce\"}", "12:00"),
                module(18L, siteId, "海流预报", "https://www.nmefc.cn/ybfw/seacurrent/Global",
                        ModuleCategoryConst.FORECAST_SERVICE, "海流", ModuleCheckTypeConst.NUMERICAL_LIST,
                        "{\"element\":\"seacurrent\",\"regioncode\":\"Global\"}", "08:00"),
                module(19L, siteId, "海洋热浪预报", "https://www.nmefc.cn/ybfw/heatWave/tensity",
                        ModuleCategoryConst.FORECAST_SERVICE, "海洋热浪", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"heatWave\"}", "08:00"),
                module(20L, siteId, "海温预报", "https://www.nmefc.cn/ybfw/seatemp/Global",
                        ModuleCategoryConst.FORECAST_SERVICE, "海温", ModuleCheckTypeConst.NUMERICAL_LIST,
                        "{\"element\":\"seatemp\",\"regioncode\":\"Global\"}", "08:00"),
                module(21L, siteId, "盐度预报", "https://www.nmefc.cn/ybfw/salinity/Global",
                        ModuleCategoryConst.FORECAST_SERVICE, "盐度", ModuleCheckTypeConst.NUMERICAL_LIST,
                        "{\"element\":\"salinity\",\"regioncode\":\"Global\"}", "08:00"),
                module(22L, siteId, "生态预报", "https://www.nmefc.cn/ybfw/styb/WestNorthPacific",
                        ModuleCategoryConst.FORECAST_SERVICE, "生态", ModuleCheckTypeConst.DATA_INIT,
                        "{\"key\":\"ecology\"}", "08:00"),
                module(23L, siteId, "中尺度诊断产品", "https://www.nmefc.cn/ybfw/diagnostic/scs",
                        ModuleCategoryConst.FORECAST_SERVICE, "中尺度诊断", ModuleCheckTypeConst.DEEPSEA_INFO,
                        "{\"region\":\"scs\",\"element\":\"eddy\"}", "08:00")
        );
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
