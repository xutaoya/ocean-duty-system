package com.oceanduty.module.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色
     */
    private String role;

    /**
     * 值班开始时间
     */
    private LocalDateTime startTime;

    /**
     * 值班结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态 0禁用 1正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
