import { postAxios } from '@/lib/axios'

/**
 * 分页查询值班日志
 */
export const queryDutyLog = (data) => {
  return postAxios('/duty/log/query', data)
}

/**
 * 新增值班日志
 */
export const addDutyLog = (data) => {
  return postAxios('/duty/log/add', data)
}

/**
 * 更新值班日志
 */
export const updateDutyLog = (data) => {
  return postAxios('/duty/log/update', data)
}
