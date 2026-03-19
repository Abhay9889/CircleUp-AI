// ── Format file size ──────────────────────────────────────
export function formatBytes(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`
}

// ── Format duration ───────────────────────────────────────
export function formatDuration(seconds) {
  if (!seconds) return '0m'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

// ── Relative time ─────────────────────────────────────────
export function timeAgo(dateStr) {
  if (!dateStr) return ''
  const diff = (Date.now() - new Date(dateStr)) / 1000
  if (diff < 60)    return 'just now'
  if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  return `${Math.floor(diff / 86400)}d ago`
}

// ── Truncate ──────────────────────────────────────────────
export function truncate(str, n = 60) {
  if (!str) return ''
  return str.length > n ? str.slice(0, n) + '…' : str
}

// ── SM-2 ease label ───────────────────────────────────────
export function efLabel(ef) {
  if (ef >= 2.5) return { label: 'Easy',   color: 'text-green-400'  }
  if (ef >= 2.0) return { label: 'Medium', color: 'text-yellow-400' }
  return             { label: 'Hard',   color: 'text-red-400'    }
}
