package com.oceanduty.module.duty.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 值班日志新增/更新参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogDTO {

    /**
     * 主键ID（更新时必填）
     */
    private Long id;

    /**
     * 值班人员
     */
    @NotBlank(message = "值班人员不能为空")
    private String userName;

    /**
     * 值班时间
     */
    @NotNull(message = "值班时间不能为空")
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
