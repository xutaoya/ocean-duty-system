package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.monitor.domain.MonitorModuleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模块监控 DAO
 */
@Mapper
public interface MonitorModuleDao extends BaseMapper<MonitorModuleEntity> {
}
