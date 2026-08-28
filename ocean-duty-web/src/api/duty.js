import { postAxios, getAxios } from '@/lib/axios'

/**
 * 获取仪表盘记录日志按钮状态
 */
export const getDutyLogSnapshotStatus = () => {
  return getAxios('/duty/log/snapshot-status')
}

/**
 * 记录当前监控异常快照
 */
export const recordDutyLogSnapshot = () => {
  return postAxios('/duty/log/record-snapshot')
}

/**
 * 值班日志详情
 */
export const getDutyLogDetail = (id) => {
  return getAxios(`/duty/log/detail/${id}`)
}

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

/**
 * 删除值班日志
 */
export const deleteDutyLog = (id) => {
  return getAxios(`/duty/log/delete/${id}`)
}
