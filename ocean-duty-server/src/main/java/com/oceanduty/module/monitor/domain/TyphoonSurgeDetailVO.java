package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 台风风暴潮四步数据链路详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyphoonSurgeDetailVO {

    /** 网站库起报时间 */
    private LocalDateTime initialTime;

    /** 网站库 update_time 记录更新时间 */
    private LocalDateTime updateTime;

    /** PG 库 done_stamp 最新时间 */
    private LocalDateTime pgDoneStamp;

    private String ftpFileName;

    private LocalDateTime ftpModifiedTime;

    private Long ftpFileSizeBytes;

    private String rawFolder;

    private String rawFileName;

    private LocalDateTime rawModifiedTime;

    private Long rawFileSizeBytes;

    private String remark;
}
