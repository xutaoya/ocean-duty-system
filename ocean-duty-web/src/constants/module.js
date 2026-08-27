import { MODULE_CATEGORY } from '@/constants/monitor'

/**
 * 检测方式常量
 */
export const MODULE_CHECK_TYPE = {
  WARN_HISTORY: {
    value: 'WARN_HISTORY',
    desc: '灾害预警历史',
    fields: [
      { key: 'warnType', label: '预警类型', placeholder: 'wave/storm/ice/tsunami' },
      { key: 'filter', label: '图片过滤', placeholder: '如 WaveWarning_TaiFeng，可留空' }
    ]
  },
  ANALYSIS_LIST: {
    value: 'ANALYSIS_LIST',
    desc: '分析/综合预报',
    fields: [{ key: 'type', label: '类型参数', placeholder: '如 waveAnalysis' }]
  },
  NUMERICAL_LIST: {
    value: 'NUMERICAL_LIST',
    desc: '数值预报',
    fields: [
      { key: 'element', label: '要素', placeholder: '如 wave/seatemp' },
      { key: 'regioncode', label: '区域', placeholder: '如 Global' }
    ]
  },
  DATA_INIT: {
    value: 'DATA_INIT',
    desc: '初始化数据',
    fields: [{ key: 'key', label: '初始化Key', placeholder: '如 typhoon' }]
  },
  DEEPSEA_INFO: {
    value: 'DEEPSEA_INFO',
    desc: '中尺度诊断',
    fields: [
      { key: 'region', label: '区域', placeholder: '如 scs' },
      { key: 'element', label: '要素', placeholder: '如 eddy' }
    ]
  },
  POLAR_REGIONS_LIST: {
    value: 'POLAR_REGIONS_LIST',
    desc: '极地预报',
    fields: [{ key: 'region', label: '区域', placeholder: '如 ArcticPolarIce' }]
  },
  CMS_FORECAST_ALARM: {
    value: 'CMS_FORECAST_ALARM',
    desc: 'CMS灾害预警表',
    fields: [
      { key: 'datasourceId', label: '数据源', inputType: 'datasource', placeholder: '请选择数据源' },
      {
        key: 'type',
        label: '警报类型',
        placeholder: 'wave/storm/bore/ice',
        options: [
          { value: 'wave', label: '海浪' },
          { value: 'storm', label: '风暴潮' },
          { value: 'bore', label: '海啸' },
          { value: 'ice', label: '海冰' }
        ]
      }
    ]
  },
  CMS_TABLE_PUBLISH: {
    value: 'CMS_TABLE_PUBLISH',
    desc: 'CMS表发布时间',
    fields: [
      { key: 'datasourceId', label: '数据源', inputType: 'datasource', placeholder: '请选择数据源' },
      { key: 'timeField', label: '时间字段', placeholder: '如 create_date' },
      { key: 'titleField', label: '标题字段', placeholder: '如 name/title' },
      {
        key: 'scheduleType',
        label: '周期类型',
        placeholder: 'daily/monthly',
        options: [
          { value: 'daily', label: '每日' },
          { value: 'monthly', label: '每月' }
        ]
      },
      { key: 'categoryId', label: '分类ID', placeholder: '月预报等可选，如 1190087852779372544' }
    ]
  },
  CMS_GRID_UPDATE: {
    value: 'CMS_GRID_UPDATE',
    desc: '智能网格表更新',
    fields: [
      { key: 'datasourceId', label: '数据源', inputType: 'datasource', placeholder: '请选择数据源' },
      { key: 'timeField', label: '时间字段', placeholder: '如 update_date' },
      {
        key: 'windowPreset',
        label: '时段规则',
        placeholder: '选择预设规则',
        options: [
          { value: 'wind', label: '风 · 13小时/时段' },
          { value: 'wave', label: '海浪 · 15h/11h' },
          { value: 'current', label: '海流 · 12h/16h' },
          { value: 'sst', label: '海温 · 12h/16h' },
          { value: 'storm_tide', label: '天文潮 · 24小时' }
        ]
      }
    ]
  }
}

export const MODULE_CATEGORY_OPTIONS = Object.values(MODULE_CATEGORY)

export default {
  MODULE_CHECK_TYPE,
  MODULE_CATEGORY_OPTIONS
}
