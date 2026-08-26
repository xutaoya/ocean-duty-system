/**
 * 用户角色常量
 */
export const USER_ROLE = {
  ADMIN: { value: 'admin', desc: '管理员' },
  DUTY: { value: 'duty', desc: '值班人员' }
}

export const USER_ROLE_OPTIONS = Object.values(USER_ROLE)

export const USER_STATUS = {
  DISABLED: { value: 0, desc: '禁用' },
  ENABLED: { value: 1, desc: '正常' }
}

export const USER_STATUS_OPTIONS = Object.values(USER_STATUS)

/**
 * 是否管理员
 */
export const isAdminRole = (role) => role === USER_ROLE.ADMIN.value
