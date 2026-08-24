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
 * 网站监控实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_site")
public class MonitorSiteEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 网站名称
     */
    private String siteName;

    /**
     * 网站地址
     */
    private String siteUrl;

    /**
     * 网站类型
     */
    private String siteType;

    /**
     * 状态 0异常 1正常 2警告
     */
    private Integer status;

    /**
     * HTTP响应状态码
     */
    private Integer httpStatus;

    /**
     * 响应时间(ms)
     */
    private Integer responseTime;

    /**
     * 最近检测时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 异常信息
     */
    private String errorMessage;

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
