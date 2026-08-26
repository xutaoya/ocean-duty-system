package com.oceanduty.module.login;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.module.login.domain.LoginDTO;
import com.oceanduty.module.login.domain.LoginVO;
import com.oceanduty.module.login.domain.SysUserEntity;
import com.oceanduty.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录服务
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final SysUserDao sysUserDao;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public ResponseDTO<LoginVO> login(LoginDTO loginDTO) {
        SysUserEntity user = sysUserDao.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUsername, loginDTO.getUsername()));

        if (user == null || !matchesPassword(user.getPassword(), loginDTO.getPassword())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResponseCodeConst.NO_PERMISSION, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return ResponseDTO.succ(LoginVO.builder()
                .token(token)
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build());
    }

    private boolean matchesPassword(String storedPassword, String rawPassword) {
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}
