package com.oceanduty.module.duty.domain;

import com.oceanduty.common.domain.PageParamDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 值班日志查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DutyLogQueryDTO extends PageParamDTO {

    /**
     * 值班人员
     */
    private String userName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
