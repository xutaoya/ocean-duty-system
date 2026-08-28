package com.oceanduty.module.duty;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.duty.domain.DutyIncidentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值班异常事件 DAO
 */
@Mapper
public interface DutyIncidentDao extends BaseMapper<DutyIncidentEntity> {
}
