import { defineStore } from 'pinia'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    // 访问令牌
    token: localStorage.getItem('token') || '',
    // 用户名
    username: localStorage.getItem('username') || '',
    // 真实姓名
    realName: localStorage.getItem('realName') || '',
    // 角色
    role: localStorage.getItem('role') || ''
  }),
  actions: {
    /**
     * 设置登录信息
     */
    setLoginInfo(loginVO) {
      this.token = loginVO.token
      this.username = loginVO.username
      this.realName = loginVO.realName
      this.role = loginVO.role
      localStorage.setItem('token', loginVO.token)
      localStorage.setItem('username', loginVO.username)
      localStorage.setItem('realName', loginVO.realName)
      localStorage.setItem('role', loginVO.role)
    },
    /**
     * 退出登录
     */
    logout() {
      this.token = ''
      this.username = ''
      this.realName = ''
      this.role = ''
      localStorage.clear()
    }
  }
})
