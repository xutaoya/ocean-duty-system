import { onMounted, onUnmounted, ref } from 'vue'

const MOBILE_BREAKPOINT = 768

export function useMobileLayout() {
  const isMobile = ref(window.innerWidth <= MOBILE_BREAKPOINT)

  const updateIsMobile = () => {
    isMobile.value = window.innerWidth <= MOBILE_BREAKPOINT
  }

  onMounted(() => {
    window.addEventListener('resize', updateIsMobile)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateIsMobile)
  })

  return { isMobile }
}
