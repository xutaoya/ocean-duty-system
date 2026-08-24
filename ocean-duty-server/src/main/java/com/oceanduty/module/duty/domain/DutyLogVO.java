package com.oceanduty.module.duty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 值班日志视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 值班人员
     */
    private String userName;

    /**
     * 值班时间
     */
    private LocalDateTime dutyTime;

    /**
     * 网站状态摘要
     */
    private String siteStatus;

    /**
     * 模块状态摘要
     */
    private String moduleStatus;

    /**
     * 故障原因
     */
    private String problem;

    /**
     * 处理措施
     */
    private String solution;

    /**
     * 恢复时间
     */
    private LocalDateTime recoverTime;
}
