const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

/** 오늘로부터 days 일 뒤의 날짜 */
export function daysFromNow(days: number): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + days);
  return date;
}

/**
 * 백엔드 날짜 문자열("2026-09-22T09:00:00", 초 뒤 마이크로초가 붙기도 함)을 Date 로 바꿉니다.
 * 시간대 표시가 없는 문자열은 이 브라우저의 현지 시간(한국)으로 읽습니다.
 */
export function parseDateTime(value: string): Date {
  return new Date(value.replace(/(\.\d{3})\d+/, '$1'));
}

/** "9월 22일(화)" */
export function formatMonthDay(date: Date): string {
  return `${date.getMonth() + 1}월 ${date.getDate()}일(${WEEKDAYS[date.getDay()]})`;
}

/** "오전 9:00" · "오후 1:30" */
export function formatTime(value: string): string {
  const date = parseDateTime(value);
  const hour = date.getHours();
  const minute = String(date.getMinutes()).padStart(2, '0');
  return `${hour < 12 ? '오전' : '오후'} ${hour % 12 || 12}:${minute}`;
}

/** "2026년 9월 22일(화) 오전 9:00" */
export function formatScheduleLabel(value: string): string {
  const date = parseDateTime(value);
  return `${date.getFullYear()}년 ${formatMonthDay(date)} ${formatTime(value)}`;
}

/** "2026.09.22(화) 오전 9:00" */
export function formatDotDateTime(value: string): string {
  const date = parseDateTime(value);
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}.${mm}.${dd}(${WEEKDAYS[date.getDay()]}) ${formatTime(value)}`;
}
