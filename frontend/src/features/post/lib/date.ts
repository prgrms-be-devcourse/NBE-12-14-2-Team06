const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

/** 오늘로부터 days 일 뒤의 날짜 */
export function daysFromNow(days: number): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + days);
  return date;
}

/** "4월 28일(월)" */
export function formatMonthDay(date: Date): string {
  return `${date.getMonth() + 1}월 ${date.getDate()}일(${WEEKDAYS[date.getDay()]})`;
}

/** "2026.10.15(목)" */
export function formatFullDate(date: Date): string {
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}.${mm}.${dd}(${WEEKDAYS[date.getDay()]})`;
}

/**
 * 백엔드 날짜 문자열("2026-10-03T09:00:00", 초 뒤 마이크로초가 붙기도 함)을 Date 로 바꿉니다.
 * 시간대 표시가 없는 문자열은 이 브라우저의 현지 시간(한국)으로 읽습니다.
 */
export function parseDateTime(value: string): Date {
  return new Date(value.replace(/(\.\d{3})\d+/, '$1'));
}

/** "방금 전" · "5분 전" · "2시간 전" · "3일 전" */
export function formatAgo(value: string, now: Date = new Date()): string {
  const minutes = Math.floor((now.getTime() - parseDateTime(value).getTime()) / 60000);
  if (minutes < 1) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}시간 전`;
  return `${Math.floor(hours / 24)}일 전`;
}

/** 오늘(0일)로부터 그 날짜까지 며칠 남았는지. 지난 날짜는 음수 */
export function daysUntil(value: string, now: Date = new Date()): number {
  const target = parseDateTime(value);
  target.setHours(0, 0, 0, 0);
  const today = new Date(now);
  today.setHours(0, 0, 0, 0);
  return Math.round((target.getTime() - today.getTime()) / 86400000);
}

/** "오전 9:00" · "오후 1:30" */
export function formatTime(value: string): string {
  const date = parseDateTime(value);
  const hour = date.getHours();
  const minute = String(date.getMinutes()).padStart(2, '0');
  return `${hour < 12 ? '오전' : '오후'} ${hour % 12 || 12}:${minute}`;
}

/** 백엔드로 보낼 "2026-10-03T09:00:00" (UTC 로 바뀌지 않게 현지 시간 그대로) */
export function toDateTimeParam(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

/** "2026.09.12   18:12" (등록일 표시용) */
export function formatDateTime(value: string): string {
  const date = parseDateTime(value);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}   ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

/** "10월 2일(금) 오전 9:00" */
export function formatMonthDayTime(value: string): string {
  const date = parseDateTime(value);
  return `${formatMonthDay(date)} ${formatTime(value)}`;
}
