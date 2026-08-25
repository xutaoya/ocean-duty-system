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
  }
}

export default {
  MONITOR_STATUS,
  MODULE_CATEGORY
}
