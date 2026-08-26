<template>
  <div :class="['site-favicon-avatar', `site-favicon-avatar--${size}`]">
    <img
      v-if="showLogo"
      :src="faviconUrl"
      :alt="name"
      class="site-favicon-avatar__img"
      @error="handleFaviconError"
    />
    <span v-else class="site-favicon-avatar__fallback">{{ initial }}</span>
  </div>
</template>

<script>
import { computed, ref, watch } from 'vue'
import { getSiteFaviconList } from '@/lib/site-favicon'

export default {
  name: 'SiteFaviconAvatar',
  props: {
    siteUrl: {
      type: String,
      default: ''
    },
    name: {
      type: String,
      default: ''
    },
    size: {
      type: String,
      default: 'md'
    }
  },
  setup(props) {
    const faviconError = ref(false)
    const faviconIndex = ref(0)
    const faviconList = computed(() => getSiteFaviconList(props.siteUrl))
    const faviconUrl = computed(() => faviconList.value[faviconIndex.value] || '')
    const showLogo = computed(() => !!faviconUrl.value && !faviconError.value)
    const initial = computed(() => (props.name || '站').charAt(0))

    watch(() => props.siteUrl, () => {
      faviconError.value = false
      faviconIndex.value = 0
    })

    const handleFaviconError = () => {
      if (faviconIndex.value < faviconList.value.length - 1) {
        faviconIndex.value += 1
        return
      }
      faviconError.value = true
    }

    return { faviconUrl, showLogo, initial, handleFaviconError }
  }
}
</script>

<style scoped>
.site-favicon-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  flex-shrink: 0;
}

.site-favicon-avatar--sm {
  width: 32px;
  height: 32px;
}

.site-favicon-avatar--md {
  width: 42px;
  height: 42px;
}

.site-favicon-avatar__img {
  width: 70%;
  height: 70%;
  object-fit: contain;
}

.site-favicon-avatar__fallback {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
}

.site-favicon-avatar--md .site-favicon-avatar__fallback {
  font-size: 16px;
}
</style>
