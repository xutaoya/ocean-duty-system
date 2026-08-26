<template>
  <el-dialog
    :model-value="modelValue"
    width="900px"
    top="4vh"
    append-to-body
    destroy-on-close
    :show-close="true"
    class="alarm-detail-dialog"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="alarm-detail-body">
      <template v-if="alarm">
        <div class="alarm-hero" :style="heroStyle">
          <div class="hero-source">{{ dialogSource }}</div>
          <h2 class="hero-title">{{ alarm.title || '-' }}</h2>
          <div class="hero-tags">
            <span v-if="alarm.alarmTypeName" class="hero-tag">{{ alarm.alarmTypeName }}</span>
            <span v-if="alarm.level" class="hero-tag hero-tag--level">{{ alarm.level }}</span>
          </div>
          <div class="hero-meta">
            <span><em>{{ codeLabel }}</em>{{ alarm.code || '-' }}</span>
            <span class="meta-sep">|</span>
            <span><em>发布时间</em>{{ formatTime(alarm.alarmDate) }}</span>
            <span class="meta-sep">|</span>
            <span><em>监测模块</em>{{ alarm.moduleName || '-' }}</span>
          </div>
        </div>

        <div v-if="alarm.image" class="alarm-section alarm-section--image">
          <div class="section-head">
            <span class="section-index">{{ sectionNumber('image') }}</span>
            <h4 class="section-title">警报图</h4>
          </div>
          <div class="section-body section-body--flat">
            <el-image
              :src="alarm.image"
              :preview-src-list="[alarm.image]"
              fit="contain"
              class="alarm-image"
            >
              <template #error>
                <div class="image-error">图片加载失败</div>
              </template>
            </el-image>
          </div>
        </div>

        <div v-if="alarm.description" class="alarm-section">
          <div class="section-head">
            <span class="section-index">{{ sectionNumber('description') }}</span>
            <h4 class="section-title">警报描述</h4>
          </div>
          <div class="section-body">
            <div class="rich-content" v-html="safeDescription" />
          </div>
        </div>

        <div v-if="alarm.defenseGuide" class="alarm-section alarm-section--guide">
          <div class="section-head">
            <span class="section-index">{{ sectionNumber('defenseGuide') }}</span>
            <h4 class="section-title">防御指南</h4>
          </div>
          <div class="section-body section-body--guide">
            <div class="rich-content" v-html="safeDefenseGuide" />
          </div>
        </div>

        <div v-if="alarm.standard" class="alarm-section">
          <div class="section-head">
            <span class="section-index">{{ sectionNumber('standard') }}</span>
            <h4 class="section-title">标准说明</h4>
          </div>
          <div class="section-body">
            <div class="rich-content" v-html="safeStandard" />
          </div>
        </div>

        <div v-if="alarm.content" class="alarm-section">
          <div class="section-head">
            <span class="section-index">{{ sectionNumber('content') }}</span>
            <h4 class="section-title">扩展内容</h4>
          </div>
          <div class="section-body">
            <div v-if="contentIsHtml" class="rich-content" v-html="safeContent" />
            <pre v-else class="plain-content">{{ alarm.content }}</pre>
          </div>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="暂无警报详情" />
    </div>
  </el-dialog>
</template>

<script>
import { computed } from 'vue'
import { getAlarmLevelTheme } from '@/lib/alarm-theme'
import { hasHtmlTag, sanitizeAlarmHtml } from '@/lib/sanitize-html'

export default {
  name: 'MonitorAlarmDetailDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    alarm: {
      type: Object,
      default: null
    }
  },
  emits: ['update:modelValue'],
  setup(props) {
    const levelTheme = computed(() => getAlarmLevelTheme(props.alarm?.level))

    const dialogSource = computed(() => {
      if (!props.alarm) return '警报详情'
      return `${props.alarm.moduleGroup || '灾害预警'} · ${props.alarm.alarmTypeName || '警报'}`
    })

    const heroStyle = computed(() => ({
      '--hero-accent': levelTheme.value.accent,
      '--hero-bg': levelTheme.value.bg,
      '--hero-color': levelTheme.value.color
    }))

    const codeLabel = computed(() => {
      if (props.alarm?.alarmType === 'bore') return '受影响海域'
      return '警报编号'
    })

    const safeDescription = computed(() => sanitizeAlarmHtml(props.alarm?.description))
    const safeDefenseGuide = computed(() => sanitizeAlarmHtml(props.alarm?.defenseGuide))
    const safeStandard = computed(() => sanitizeAlarmHtml(props.alarm?.standard))
    const safeContent = computed(() => sanitizeAlarmHtml(props.alarm?.content))
    const contentIsHtml = computed(() => hasHtmlTag(props.alarm?.content))

    const sectionKeys = computed(() => {
      const keys = []
      if (props.alarm?.image) keys.push('image')
      if (props.alarm?.description) keys.push('description')
      if (props.alarm?.defenseGuide) keys.push('defenseGuide')
      if (props.alarm?.standard) keys.push('standard')
      if (props.alarm?.content) keys.push('content')
      return keys
    })

    const sectionNumber = (key) => {
      const index = sectionKeys.value.indexOf(key)
      return index >= 0 ? String(index + 1).padStart(2, '0') : '00'
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    return {
      dialogSource,
      heroStyle,
      codeLabel,
      safeDescription,
      safeDefenseGuide,
      safeStandard,
      safeContent,
      contentIsHtml,
      sectionNumber,
      formatTime
    }
  }
}
</script>

<style scoped>
.alarm-detail-body {
  max-height: calc(86vh - 80px);
  overflow-y: auto;
}

.alarm-hero {
  margin: -20px -20px 28px;
  padding: 24px 28px 20px;
  background: linear-gradient(135deg, var(--hero-bg) 0%, #fff 100%);
  border-bottom: 3px solid var(--hero-accent);
}

.hero-source {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.hero-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.4;
  word-break: break-word;
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.hero-tag {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.8);
  color: #595959;
  border: 1px solid #f0f0f0;
}

.hero-tag--level {
  color: var(--hero-color);
  border-color: var(--hero-accent);
  background: #fff;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 4px;
  font-size: 14px;
  color: #434343;
}

.hero-meta em {
  font-style: normal;
  color: #bfbfbf;
  margin-right: 6px;
}

.meta-sep {
  color: #e8e8e8;
  margin: 0 4px;
}

.alarm-section {
  margin-bottom: 28px;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.section-index {
  font-size: 12px;
  font-weight: 700;
  color: var(--hero-accent, #1890ff);
  letter-spacing: 1px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.section-body {
  padding: 18px 20px;
  background: #fafbfc;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.section-body--flat {
  padding: 12px;
  background: #fff;
}

.section-body--guide {
  background: #fffbe6;
  border-color: #ffe58f;
}

.rich-content {
  font-size: 15px;
  line-height: 1.9;
  color: #434343;
  word-break: break-word;
}

.rich-content :deep(p) {
  margin: 0 0 14px;
  text-align: justify;
  text-indent: 2em;
}

.rich-content :deep(p:last-child) {
  margin-bottom: 0;
}

.rich-content :deep(strong),
.rich-content :deep(b) {
  color: #1a1a2e;
  font-weight: 600;
}

.rich-content :deep(br) {
  display: block;
  margin-bottom: 8px;
  content: '';
}

.plain-content {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #595959;
  white-space: pre-wrap;
  word-break: break-all;
}

.alarm-image {
  width: 100%;
  max-height: 460px;
  border-radius: 6px;
  background: #fafafa;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 160px;
  color: #bfbfbf;
  background: #f5f5f5;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 20px;
  }

  .hero-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .meta-sep {
    display: none;
  }

  .section-body {
    padding: 14px 16px;
  }

  .rich-content :deep(p) {
    text-indent: 1.5em;
  }
}
</style>

<style>
.alarm-detail-dialog .el-dialog__header {
  display: none;
}

.alarm-detail-dialog .el-dialog__body {
  padding: 20px;
}
</style>
