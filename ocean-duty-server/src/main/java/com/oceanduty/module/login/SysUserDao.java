package com.oceanduty.module.login;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oceanduty.module.login.domain.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 DAO
 */
@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {
}
