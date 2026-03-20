export const IOA_DISPLAY_NAMES = { M81496: '刘秋诗', M20506: '赵轶群', M00828: '丁滢' }

export function displayName(name) {
  return IOA_DISPLAY_NAMES[name?.toUpperCase()] || name || '—'
}

export function priorityType(p) {
  return { '紧急': 'danger', '高': 'warning', '中': 'primary', '低': 'info' }[p] || 'info'
}

export function formatDatetime(val) {
  if (!val) return '—'
  const d = new Date(val)
  if (isNaN(d)) return val
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
