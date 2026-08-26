package com.oceanduty.module.user;

import com.oceanduty.common.anno.RequireRole;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.constant.UserRoleConst;
import com.oceanduty.module.user.domain.UserDTO;
import com.oceanduty.module.user.domain.UserQueryDTO;
import com.oceanduty.module.user.domain.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口
 */
@Tag(name = "用户管理")
@RestController
@RequireRole(UserRoleConst.ADMIN)
@RequiredArgsConstructor
public class UserController {

    private final UserManageService userManageService;

    @Operation(summary = "分页查询用户 @author ocean-duty")
    @PostMapping("/user/query")
    public ResponseDTO<PageResultVO<UserVO>> queryUser(@Valid @RequestBody UserQueryDTO queryDTO) {
        return userManageService.queryUser(queryDTO);
    }

    @Operation(summary = "查询用户详情 @author ocean-duty")
    @GetMapping("/user/get/{id}")
    public ResponseDTO<UserVO> getUser(@PathVariable Long id) {
        return userManageService.getUser(id);
    }

    @Operation(summary = "新增用户 @author ocean-duty")
    @PostMapping("/user/add")
    public ResponseDTO<String> addUser(@Valid @RequestBody UserDTO userDTO) {
        return userManageService.addUser(userDTO);
    }

    @Operation(summary = "更新用户 @author ocean-duty")
    @PostMapping("/user/update")
    public ResponseDTO<String> updateUser(@Valid @RequestBody UserDTO userDTO) {
        return userManageService.updateUser(userDTO);
    }

    @Operation(summary = "删除用户 @author ocean-duty")
    @GetMapping("/user/delete/{id}")
    public ResponseDTO<String> deleteUser(@PathVariable Long id) {
        return userManageService.deleteUser(id);
    }
}
