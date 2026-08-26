package com.oceanduty.module.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.constant.UserRoleConst;
import com.oceanduty.module.login.SysUserDao;
import com.oceanduty.module.login.domain.SysUserEntity;
import com.oceanduty.module.user.domain.UserDTO;
import com.oceanduty.module.user.domain.UserQueryDTO;
import com.oceanduty.module.user.domain.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 */
@Service
@RequiredArgsConstructor
public class UserManageService {

    private static final Set<String> ALLOWED_ROLES = Set.of(UserRoleConst.ADMIN, UserRoleConst.DUTY);

    private final SysUserDao sysUserDao;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户
     */
    public ResponseDTO<PageResultVO<UserVO>> queryUser(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(SysUserEntity::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(SysUserEntity::getRole, queryDTO.getRole());
        }
        wrapper.orderByAsc(SysUserEntity::getId);

        Page<SysUserEntity> page = sysUserDao.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<UserVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return ResponseDTO.succ(PageResultVO.<UserVO>builder()
                .total(page.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
                .list(list)
                .build());
    }

    /**
     * 查询用户详情
     */
    public ResponseDTO<UserVO> getUser(Long id) {
        SysUserEntity entity = sysUserDao.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        return ResponseDTO.succ(toVO(entity));
    }

    /**
     * 新增用户
     */
    public ResponseDTO<String> addUser(UserDTO userDTO) {
        validateRole(userDTO.getRole());
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "密码不能为空");
        }
        Long count = sysUserDao.selectCount(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUsername, userDTO.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "用户名已存在");
        }

        SysUserEntity entity = toEntity(userDTO);
        entity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        entity.setStatus(userDTO.getStatus() == null ? 1 : userDTO.getStatus());
        sysUserDao.insert(entity);
        return ResponseDTO.succ();
    }

    /**
     * 更新用户
     */
    public ResponseDTO<String> updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "用户ID不能为空");
        }
        validateRole(userDTO.getRole());

        SysUserEntity exist = sysUserDao.selectById(userDTO.getId());
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }

        SysUserEntity duplicate = sysUserDao.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUsername, userDTO.getUsername())
                .ne(SysUserEntity::getId, userDTO.getId()));
        if (duplicate != null) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "用户名已存在");
        }

        SysUserEntity entity = toEntity(userDTO);
        entity.setId(userDTO.getId());
        entity.setStatus(userDTO.getStatus() == null ? exist.getStatus() : userDTO.getStatus());
        if (StringUtils.hasText(userDTO.getPassword())) {
            entity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            entity.setPassword(exist.getPassword());
        }
        sysUserDao.updateById(entity);
        return ResponseDTO.succ();
    }

    /**
     * 删除用户
     */
    public ResponseDTO<String> deleteUser(Long id) {
        SysUserEntity exist = sysUserDao.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResponseCodeConst.NOT_FOUND);
        }
        if (exist.getId() != null && exist.getId() == 1L) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "默认管理员账号不可删除");
        }
        sysUserDao.deleteById(id);
        return ResponseDTO.succ();
    }

    private void validateRole(String role) {
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BusinessException(ResponseCodeConst.ERROR_PARAM, "角色不合法");
        }
    }

    private UserVO toVO(SysUserEntity entity) {
        return UserVO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .realName(entity.getRealName())
                .role(entity.getRole())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

    private SysUserEntity toEntity(UserDTO dto) {
        return SysUserEntity.builder()
                .username(dto.getUsername())
                .realName(dto.getRealName())
                .role(dto.getRole())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}
