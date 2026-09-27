const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

/**
 * 백엔드 날짜 문자열("2026-09-22T12:30:00", 초 뒤 마이크로초가 붙기도 함)을 Date 로 바꿉니다.
 * 시간대 표시가 없는 문자열은 이 브라우저의 현지 시간(한국)으로 읽습니다.
 */
function parseDateTime(value: string): Date {
  return new Date(value.replace(/(\.\d{3})\d+/, '$1'));
}

/** "2026.09.22(화)" */
export function formatDotDate(value: string): string {
  const date = parseDateTime(value);
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}.${mm}.${dd}(${WEEKDAYS[date.getDay()]})`;
}

/** "오전 9:00" · "오후 12:30" */
export function formatTime(value: string): string {
  const date = parseDateTime(value);
  const hour = date.getHours();
  const minute = String(date.getMinutes()).padStart(2, '0');
  return `${hour < 12 ? '오전' : '오후'} ${hour % 12 || 12}:${minute}`;
}

/** "2026.09.22(화) 오후 12:30" */
export function formatDateTime(value: string): string {
  return `${formatDotDate(value)} ${formatTime(value)}`;
}

/** "9:00 ~ 12:10" (동행 시간대) */
export function formatTimeRange(start: string, end: string): string {
  return `${formatClock(start)} ~ ${formatClock(end)}`;
}

/** "9:00" */
function formatClock(value: string): string {
  const date = parseDateTime(value);
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
}
