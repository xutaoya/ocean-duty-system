import { getAxios, postAxios } from '@/lib/axios'

/**
 * 获取监控仪表盘数据
 */
export const getDashboard = () => {
  return getAxios('/monitor/dashboard/get')
}

/**
 * 查询全部网站监控
 */
export const listSites = () => {
  return getAxios('/monitor/site/list')
}

/**
 * 查询全部模块监控
 */
export const listModules = () => {
  return getAxios('/monitor/module/list')
}

/**
 * 手动触发网站检测
 */
export const checkSites = () => {
  return postAxios('/monitor/site/check')
}

/**
 * 手动触发模块检测
 */
export const checkModules = () => {
  return postAxios('/monitor/module/check')
}

/**
 * 查询模块最新 CMS 警报详情
 */
export const getModuleAlarmDetail = (id) => {
  return getAxios(`/monitor/module/alarm/${id}`)
}
