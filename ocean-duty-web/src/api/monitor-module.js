import { getAxios, postAxios } from '@/lib/axios'

/**
 * 分页查询监控模块
 */
export const queryModule = (data) => {
  return postAxios('/monitor/module/query', data)
}

/**
 * 查询监控模块详情
 */
export const getModule = (id) => {
  return getAxios('/monitor/module/get/' + id)
}

/**
 * 新增监控模块
 */
export const addModule = (data) => {
  return postAxios('/monitor/module/add', data)
}

/**
 * 更新监控模块
 */
export const updateModule = (data) => {
  return postAxios('/monitor/module/update', data)
}

/**
 * 删除监控模块
 */
export const deleteModule = (id) => {
  return getAxios('/monitor/module/delete/' + id)
}
