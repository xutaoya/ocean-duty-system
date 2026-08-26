/**
 * 根据网站地址获取 favicon 候选列表
 */
export const getSiteFaviconList = (siteUrl, size = 64) => {
  if (!siteUrl) return []
  try {
    const url = new URL(siteUrl)
    return [
      `${url.origin}/favicon.ico`,
      `https://www.google.com/s2/favicons?domain=${url.hostname}&sz=${size}`
    ]
  } catch {
    return []
  }
}
