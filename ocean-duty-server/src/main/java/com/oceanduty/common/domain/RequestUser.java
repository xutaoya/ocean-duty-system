package com.oceanduty.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUser {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色
     */
    private String role;
}
