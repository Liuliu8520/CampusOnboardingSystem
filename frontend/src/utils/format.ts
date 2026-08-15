/**
 * 将后端返回的时间字符串格式化为中文日期。
 *
 * 后端 LocalDateTime 默认序列化为 ISO 格式（如 `2026-08-07T04:20:59`），
 * 经 spring.jackson.date-format 处理后也可能为 `2026-08-07 04:20:59`。
 * 统一格式化为 `2026年08月07日`。传入空值返回空字符串。
 */
export function formatDate(value?: string | null): string {
  if (!value) {
    return ''
  }
  const normalized = value.replace('T', ' ')
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(normalized)
  if (!match) {
    return value
  }
  const [, year, month, day] = match
  return `${year}年${month}月${day}日`
}
