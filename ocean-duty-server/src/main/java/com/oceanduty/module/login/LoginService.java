package com.oceanduty.module.login;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.module.login.domain.LoginDTO;
import com.oceanduty.module.login.domain.LoginVO;
import com.oceanduty.module.login.domain.SysUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 登录服务
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final SysUserDao sysUserDao;

    /**
     * 用户登录
     */
    public ResponseDTO<LoginVO> login(LoginDTO loginDTO) {
        SysUserEntity user = sysUserDao.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUsername, loginDTO.getUsername()));

        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResponseCodeConst.NO_PERMISSION, "账号已被禁用");
        }

        // TODO ocean-duty: 替换为 JWT 令牌
        String token = UUID.randomUUID().toString().replace("-", "");

        return ResponseDTO.succ(LoginVO.builder()
                .token(token)
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build());
    }
}
