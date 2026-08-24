package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.monitor.domain.MonitorSiteEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 网站监控 DAO
 */
@Mapper
public interface MonitorSiteDao extends BaseMapper<MonitorSiteEntity> {
}
