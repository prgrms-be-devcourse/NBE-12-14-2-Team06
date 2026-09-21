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
