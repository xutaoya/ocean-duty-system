package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.duty.domain.DutyLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值班日志 DAO
 */
@Mapper
public interface DutyLogDao extends BaseMapper<DutyLogEntity> {
}
