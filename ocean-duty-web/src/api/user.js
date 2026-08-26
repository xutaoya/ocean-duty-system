import { postAxios, getAxios } from '@/lib/axios'

/**
 * 分页查询用户
 */
export const queryUser = (data) => {
  return postAxios('/user/query', data)
}

/**
 * 查询用户详情
 */
export const getUser = (id) => {
  return getAxios(`/user/get/${id}`)
}

/**
 * 新增用户
 */
export const addUser = (data) => {
  return postAxios('/user/add', data)
}

/**
 * 更新用户
 */
export const updateUser = (data) => {
  return postAxios('/user/update', data)
}

/**
 * 删除用户
 */
export const deleteUser = (id) => {
  return getAxios(`/user/delete/${id}`)
}
