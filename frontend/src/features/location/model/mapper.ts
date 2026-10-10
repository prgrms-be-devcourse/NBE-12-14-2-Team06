import type { EscortLocation, EscortLocationDto } from '../types';

/** 시각을 "오후 3:25:10" 처럼 보여줍니다. 읽을 수 없는 값이면 빈 문자열. */
export function formatClock(value: Date | string): string {
  const date = typeof value === 'string' ? new Date(value) : value;
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleTimeString('ko-KR', { hour: 'numeric', minute: '2-digit', second: '2-digit' });
}

/** 서버가 준 위치(EscortLocationDto)를 화면용(EscortLocation)으로 바꿉니다. */
export function toEscortLocation(dto: EscortLocationDto): EscortLocation {
  return {
    position: { lat: dto.lat, lng: dto.lng },
    updatedAtLabel: formatClock(dto.updatedAt),
  };
}
