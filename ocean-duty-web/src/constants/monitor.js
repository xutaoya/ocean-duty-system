export const MONITOR_STATUS = {
  ERROR: {
    value: 0,
    desc: '异常',
    color: '#F56C6C'
  },
  NORMAL: {
    value: 1,
    desc: '正常',
    color: '#67C23A'
  },
  WARNING: {
    value: 2,
    desc: '警告',
    color: '#E6A23C'
  }
}

export const MODULE_CATEGORY = {
  DISASTER_WARNING: {
    value: 'disaster_warning',
    desc: '灾害预警'
  },
  FORECAST_SERVICE: {
    value: 'forecast_service',
    desc: '预报服务'
  },
  ENV_FORECAST: {
    value: 'env_forecast',
    desc: '环境预报'
  }
}

/**
 * 根据 HTTP 状态码返回样式类名
 */
export const getHttpStatusClass = (httpStatus) => {
  if (httpStatus == null || httpStatus === 0) return 'http-unknown'
  const code = Number(httpStatus)
  if (Number.isNaN(code)) return 'http-unknown'
  if (code >= 200 && code < 300) return 'http-success'
  if (code >= 300 && code < 400) return 'http-redirect'
  if (code >= 400 && code < 500) return 'http-client-error'
  if (code >= 500) return 'http-server-error'
  return 'http-unknown'
}

export default {
  MONITOR_STATUS,
  MODULE_CATEGORY,
  getHttpStatusClass
}
