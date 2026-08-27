package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CMS 表发布时间查询结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsTablePublishRecord {

    /**
     * 标题/名称
     */
    private String title;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
}
