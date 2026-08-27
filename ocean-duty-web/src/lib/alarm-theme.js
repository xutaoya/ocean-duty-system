/**
 * 根据警报等级返回主题色
 */
export const getAlarmLevelTheme = (level = '') => {
  const text = String(level || '')
  if (!text) {
    return { accent: '#d9d9d9', color: '#8c8c8c', bg: '#fafafa', tagType: 'info', name: 'default' }
  }
  if (text.includes('红') || /Ⅰ|I级/.test(text)) {
    return { accent: '#ff4d4f', color: '#cf1322', bg: '#fff1f0', tagType: 'danger', name: 'red' }
  }
  if (text.includes('橙') || /Ⅱ|II级/.test(text)) {
    return { accent: '#fa8c16', color: '#d46b08', bg: '#fff7e6', tagType: 'warning', name: 'orange' }
  }
  if (text.includes('黄') || /Ⅲ|III级/.test(text)) {
    return { accent: '#fadb14', color: '#d4b106', bg: '#fffbe6', tagType: 'warning', name: 'yellow' }
  }
  if (text.includes('蓝') || /Ⅳ|IV级/.test(text)) {
    return { accent: '#1890ff', color: '#096dd9', bg: '#e6f4ff', tagType: 'primary', name: 'blue' }
  }
  if (text.includes('解除')) {
    return { accent: '#52c41a', color: '#389e0d', bg: '#f6ffed', tagType: 'success', name: 'clear' }
  }
  return { accent: '#13c2c2', color: '#08979c', bg: '#e6fffb', tagType: 'info', name: 'info' }
}

/**
 * 根据监控状态返回主题色
 */
export const getMonitorStatusTheme = (status, MONITOR_STATUS) => {
  if (status === MONITOR_STATUS.ERROR.value || status === MONITOR_STATUS.WARNING.value) {
    return { label: '异常', className: 'is-error', color: '#ff4d4f', bg: '#fff1f0' }
  }
  return { label: '正常', className: 'is-normal', color: '#52c41a', bg: '#f6ffed' }
}
