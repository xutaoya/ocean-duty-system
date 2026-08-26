package com.oceanduty.module.user.domain;

import com.oceanduty.common.domain.PageParamDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageParamDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色
     */
    private String role;
}
