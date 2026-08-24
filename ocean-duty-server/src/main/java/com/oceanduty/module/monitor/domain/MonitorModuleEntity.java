package com.oceanduty.module.monitor.domain;

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
 * 模块监控实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_module")
public class MonitorModuleEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联网站ID
     */
    private Long siteId;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 模块页面地址
     */
    private String moduleUrl;

    /**
     * 数据更新时间
     */
    private LocalDateTime dataUpdateTime;

    /**
     * 预期更新时间(HH:mm)
     */
    private String expectedTime;

    /**
     * 状态 0异常 1正常 2警告
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 最近检查时间
     */
    private LocalDateTime lastCheckTime;

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
