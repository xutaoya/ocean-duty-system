package com.oceanduty.module.user.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户新增/更新参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * 主键ID（更新时必填）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（新增必填，更新时留空表示不修改）
     */
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 角色 admin管理员 duty值班人员
     */
    @NotBlank(message = "角色不能为空")
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
}
