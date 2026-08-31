import { MONITOR_STATUS, MODULE_CATEGORY } from '@/constants/monitor'
import { MODULE_CHECK_TYPE } from '@/constants/module'

const parseCheckParam = (checkParam) => {
  if (!checkParam) return {}
  try {
    return JSON.parse(checkParam)
  } catch {
    return {}
  }
}

export const isDisasterWarningModule = (mod) =>
  mod?.moduleCategory === MODULE_CATEGORY.DISASTER_WARNING.value

const isPublishedToday = (time) => {
  if (!time) return false
  const publishDate = time.substring(0, 10)
  const today = new Date()
  const todayStr = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, '0'),
    String(today.getDate()).padStart(2, '0')
  ].join('-')
  return publishDate === todayStr
}

const titleMatchesCurrentMonth = (title) => {
  if (!title) return false
  const match = title.match(/(\d{4})年0?(\d{1,2})月/)
  if (!match) return false
  const now = new Date()
  return Number(match[1]) === now.getFullYear() && Number(match[2]) === now.getMonth() + 1
}

const isPublishedCurrentPeriod = (mod) => {
  const scheduleType = parseCheckParam(mod.checkParam).scheduleType
  if (scheduleType === 'monthly') {
    if (titleMatchesCurrentMonth(mod.alarmTitle)) return true
    if (!mod.updateTime) return false
    const publishDate = new Date(mod.updateTime)
    const now = new Date()
    return publishDate.getFullYear() === now.getFullYear()
      && publishDate.getMonth() === now.getMonth()
  }
  return isPublishedToday(mod.updateTime)
}

const isContentCurrent = (mod) => {
  const isCmsTablePublish = mod.checkType === MODULE_CHECK_TYPE.CMS_TABLE_PUBLISH.value
  return isCmsTablePublish ? isPublishedCurrentPeriod(mod) : isPublishedToday(mod.updateTime)
}

export const resolveModuleEffectiveStatus = (mod) => {
  if (!mod || mod.status == null) {
    return MONITOR_STATUS.NORMAL.value
  }
  if (isDisasterWarningModule(mod)) {
    return MONITOR_STATUS.NORMAL.value
  }
  if (mod.checkType === MODULE_CHECK_TYPE.CMS_GRID_UPDATE.value) {
    return mod.status
  }
  if (isContentCurrent(mod)) {
    return MONITOR_STATUS.NORMAL.value
  }
  return mod.status
}

const bumpStatus = (stats, status) => {
  if (status === MONITOR_STATUS.ERROR.value) {
    stats.error += 1
  } else if (status === MONITOR_STATUS.WARNING.value) {
    stats.warning += 1
  } else {
    stats.normal += 1
  }
}

export const aggregateDashboardStats = (sites = [], modules = []) => {
  const stats = { total: 0, normal: 0, warning: 0, error: 0 }
  sites.forEach((site) => {
    stats.total += 1
    bumpStatus(stats, site.status)
  })
  modules.forEach((mod) => {
    stats.total += 1
    bumpStatus(stats, resolveModuleEffectiveStatus(mod))
  })
  return stats
}
