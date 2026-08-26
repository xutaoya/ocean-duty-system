package com.oceanduty.module.monitor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模块数据源 DAO
 */
@Mapper
public interface MonitorDatasourceDao extends BaseMapper<MonitorDatasourceEntity> {
}
