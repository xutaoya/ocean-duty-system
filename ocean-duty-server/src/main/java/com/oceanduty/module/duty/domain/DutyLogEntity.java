package com.oceanduty.module.duty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 值班日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("duty_log")
public class DutyLogEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deletedFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
