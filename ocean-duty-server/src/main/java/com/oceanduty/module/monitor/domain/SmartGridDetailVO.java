package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能网格扩展信息（起报时间、FTP 文件时间）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartGridDetailVO {

    private String elementKey;

    private String elementName;

    private String pgVersion;

    private LocalDateTime reportStartTime;

    private LocalDateTime outputDataTime;

    private LocalDateTime outputModifiedTime;

    private String outputFileName;

    private Long outputFileSizeBytes;

    private LocalDateTime elementDataTime;

    private LocalDateTime elementModifiedTime;

    private String elementFileName;

    private Long elementFileSizeBytes;

    private String elementFolder;

    private String remark;

    /**
     * 联动刷新的模块 ID（如海流与海温）
     */
    private List<Long> linkedModuleIds;

    /**
     * 是否展示 Output 区块（天文潮仅起报+要素）
     */
    private Boolean showOutput;
}
