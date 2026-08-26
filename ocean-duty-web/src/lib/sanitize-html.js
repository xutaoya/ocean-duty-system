const ALLOWED_TAG_PATTERN = /^<\/?(p|br|strong|b|em|i|ul|ol|li|span|div|h[1-6])(\s[^>]*)?>$/i

/**
 * 清理警报富文本，仅保留安全标签
 */
export const sanitizeAlarmHtml = (html) => {
  if (!html) return ''

  let safe = html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')
    .replace(/\s+on\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    .replace(/javascript:/gi, '')

  safe = safe.replace(/<\/?([a-z0-9]+)([^>]*)>/gi, (match) => {
    const normalized = match.toLowerCase()
    if (normalized.startsWith('<br')) return '<br>'
    if (ALLOWED_TAG_PATTERN.test(match)) {
      return match.replace(/\s+(style|class|id)\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    }
    return ''
  })

  return safe.trim()
}

/**
 * 判断内容是否包含 HTML 标签
 */
export const hasHtmlTag = (text) => /<[a-z][\s\S]*>/i.test(text || '')
