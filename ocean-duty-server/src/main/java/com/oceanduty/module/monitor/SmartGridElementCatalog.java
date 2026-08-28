package com.oceanduty.module.monitor;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 智能网格要素 FTP 路径与文件名规则
 */
public final class SmartGridElementCatalog {

    private SmartGridElementCatalog() {
    }

    public enum ElementLayout {
        /** 要素目录下按 yyyyMMddHH 子文件夹存放，如风 */
        SUBDIR,
        /** 要素文件直接放在目录下，如海浪 wave_xxx_yyyyMMddHH.nc */
        FLAT
    }

    public record ElementDef(
            String key,
            String name,
            long pgDatasourceId,
            String outputDir,
            String outputPrefix,
            String elementDir,
            String elementFilePrefix,
            ElementLayout elementLayout,
            String detailGroup,
            boolean scanOutput,
            String elementFileSuffix,
            Pattern outputTimePattern,
            Pattern elementTimePattern
    ) {
        public ElementDef {
            if (elementFileSuffix == null || elementFileSuffix.isBlank()) {
                elementFileSuffix = ".nc";
            }
        }
    }

    private static final Pattern TEN_DIGIT = Pattern.compile("(\\d{10})");
    private static final Pattern TWELVE_DIGIT = Pattern.compile("(\\d{12})");

    public static final List<ElementDef> ELEMENTS = List.of(
            element("wind", "风", 5L, "OutputData/Wind", "advancedWindGrid_", "Wind", "",
                    ElementLayout.SUBDIR, "", true, ".nc",
                    Pattern.compile("advancedWindGrid_(\\d{10})_"),
                    Pattern.compile("ws_10km_expect_(\\d{12})_")),
            element("wave", "海浪", 6L, "OutputData/Wave", "advancedWaveGrid_", "Wave", "wave_wve_",
                    ElementLayout.FLAT, "", true, ".nc",
                    Pattern.compile("advancedWaveGrid_(\\d{10})_(\\d{14})\\.nc$"),
                    Pattern.compile("wave_wve_(\\d{10})\\.nc$")),
            element("current", "海流", 7L, "OutputData/Circulation", "advancedCirculationGrid_", "Circulation", "FishGrid_",
                    ElementLayout.FLAT, "circulation", true, ".nc",
                    Pattern.compile("advancedCirculationGrid_(\\d{10})_"),
                    Pattern.compile("FishGrid_(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})_")),
            element("sst", "海温", 8L, "OutputData/Circulation", "advancedCirculationGrid_", "Circulation", "FishGrid_",
                    ElementLayout.FLAT, "circulation", true, ".nc",
                    Pattern.compile("advancedCirculationGrid_(\\d{10})_"),
                    Pattern.compile("FishGrid_(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})_SST\\.nc$")),
            element("storm_tide", "风暴增水", 9L, "", "", "Tide", "cswd",
                    ElementLayout.FLAT, "", false, ".txt",
                    null,
                    Pattern.compile("cswd(\\d{10})\\.txt$"))
    );

    private static ElementDef element(String key, String name, long pgDatasourceId,
                                      String outputDir, String outputPrefix, String elementDir,
                                      String elementFilePrefix, ElementLayout elementLayout, String detailGroup,
                                      boolean scanOutput, String elementFileSuffix,
                                      Pattern outputTimePattern, Pattern elementTimePattern) {
        return new ElementDef(key, name, pgDatasourceId, outputDir, outputPrefix, elementDir, elementFilePrefix,
                elementLayout, detailGroup, scanOutput, elementFileSuffix, outputTimePattern, elementTimePattern);
    }

    public static String resolveDetailGroup(String elementKey) {
        return ELEMENTS.stream()
                .filter(element -> element.key().equals(elementKey))
                .map(ElementDef::detailGroup)
                .filter(group -> group != null && !group.isBlank())
                .findFirst()
                .orElse(elementKey);
    }

    /**
     * 处理前时间在展示时需叠加的小时偏移（风/浪/风暴增水 +20h）
     */
    public static int elementDataTimeOffsetHours(String elementKey) {
        if ("wind".equals(elementKey) || "wave".equals(elementKey) || "storm_tide".equals(elementKey)) {
            return 20;
        }
        return 0;
    }
}
