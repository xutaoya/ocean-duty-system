import { postAxios } from '@/lib/axios'

/**
 * 用户登录
 */
export const login = (data) => {
  return postAxios('/login/do', data)
}
