import { getAxios, postAxios } from '@/lib/axios'

/**
 * 分页查询网站
 */
export const querySite = (data) => {
  return postAxios('/monitor/site/query', data)
}

/**
 * 查询网站详情
 */
export const getSite = (id) => {
  return getAxios('/monitor/site/get/' + id)
}

/**
 * 新增网站
 */
export const addSite = (data) => {
  return postAxios('/monitor/site/add', data)
}

/**
 * 更新网站
 */
export const updateSite = (data) => {
  return postAxios('/monitor/site/update', data)
}

/**
 * 删除网站
 */
export const deleteSite = (id) => {
  return getAxios('/monitor/site/delete/' + id)
}
