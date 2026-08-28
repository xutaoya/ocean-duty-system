package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.duty.domain.DutyLogItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值班日志明细 DAO
 */
@Mapper
public interface DutyLogItemDao extends BaseMapper<DutyLogItemEntity> {
}
