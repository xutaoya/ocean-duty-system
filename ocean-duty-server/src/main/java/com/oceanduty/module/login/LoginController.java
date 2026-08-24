package com.oceanduty.module.login;

import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.module.login.domain.LoginDTO;
import com.oceanduty.module.login.domain.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口
 */
@Tag(name = "登录认证")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @Operation(summary = "用户登录 @author ocean-duty")
    @PostMapping("/login/do")
    public ResponseDTO<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return loginService.login(loginDTO);
    }
}
