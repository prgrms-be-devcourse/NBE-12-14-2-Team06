import type { PostFormValues } from '../types';

/** 공고 작성 폼의 선택지와 계산 도우미입니다. */

/** 00:00 ~ 23:30, 30분 간격 */
export const TIME_OPTIONS: string[] = Array.from({ length: 48 }, (_, index) => {
  const hour = String(Math.floor(index / 2)).padStart(2, '0');
  return `${hour}:${index % 2 ? '30' : '00'}`;
});

export const TRANSPORT_OPTIONS = ['도보', '택시', '자가용', '대중교통', '휠체어 택시(콜택시)', '기타'];
export const PARTY_OPTIONS = ['1명 (본인만)', '2명 (보호자 동반)', '3명 이상'];

export const EMPTY_FORM: PostFormValues = {
  title: '',
  hospitalName: '',
  hospitalAddress: '',
  hospitalLat: null,
  hospitalLng: null,
  region: '',
  district: '',
  departure: '',
  pickupLat: null,
  pickupLng: null,
  date: '',
  startTime: '',
  endTime: '',
  hourlyPay: '',
  negotiable: false,
  transportOut: '',
  transportBack: '',
  party: '',
  reportRequested: false,
  description: '',
  note: '',
  recruitStartDate: '',
  recruitStartTime: '',
  recruitEndDate: '',
  recruitEndTime: '',
};

function toMinutes(time: string): number {
  const [hour, minute] = time.split(':').map(Number);
  return hour * 60 + minute;
}

/** 시작~종료 시각 사이의 분. 종료가 시작보다 늦지 않으면 0 */
export function diffMinutes(start: string, end: string): number {
  if (!start || !end) return 0;
  return Math.max(0, toMinutes(end) - toMinutes(start));
}

/** 90 → "1시간 30분" */
export function formatMinutes(minutes: number): string {
  if (minutes <= 0) return '';
  const hour = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return [hour ? `${hour}시간` : '', rest ? `${rest}분` : ''].filter(Boolean).join(' ');
}

/** 시급 × 시간 (원, 정수). 시급을 알 수 없으면 0 */
export function estimateAmount(hourlyPay: string, minutes: number): number {
  const pay = Number(hourlyPay.replace(/[^0-9]/g, ''));
  return Math.round((pay * minutes) / 60);
}

/** 숫자만 남겨서 정수로 바꿉니다. "14,000" → 14000 */
export function parsePay(hourlyPay: string): number {
  return Number(hourlyPay.replace(/[^0-9]/g, '')) || 0;
}

/** "2026-09-20" + "10:00" → "2026-09-20T10:00:00" (백엔드 LocalDateTime 포맷) */
export function toIsoDateTime(date: string, time: string): string {
  return `${date}T${time}:00`;
}

/** 오늘 날짜를 <input type="date"> 가 먹는 "YYYY-MM-DD" 로 (로컬 기준) */
export function todayDateString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/** 지금 시각을 TIME_OPTIONS(30분 간격) 형식에 맞춰 30분 단위로 내림한 "HH:mm" */
export function flooredNowTime(): string {
  const now = new Date();
  const hour = String(now.getHours()).padStart(2, '0');
  const minute = now.getMinutes() < 30 ? '00' : '30';
  return `${hour}:${minute}`;
}

/**
 * 선택한 날짜(date)가 오늘이면 "지금 이후"만 고를 수 있도록 막아야 하는 기준 시각을 돌려줍니다.
 * 이 시각 이하(<=)인 TIME_OPTIONS 는 선택할 수 없게 막으면 됩니다.
 * 오늘보다 미래 날짜면 시간 제한이 없어 빈 문자열, 과거 날짜면(날짜 자체를 min 으로 막지만 방어적으로) 전부 막습니다.
 */
export function minSelectableTime(date: string): string {
  const today = todayDateString();
  if (!date || date > today) return '';
  if (date < today) return '23:30';
  return flooredNowTime();
}

/** "2026-09-20" + "10:00" → "2026-09-20T10:00" (비교용 문자열. 날짜·시간 중 하나라도 없으면 빈 문자열) */
export function combineDateTime(date: string, time: string): string {
  return date && time ? `${date}T${time}` : '';
}

/**
 * date 가 boundDate 와 같은 날일 때, boundTime 이하(<=)인 TIME_OPTIONS 를 막습니다 (= boundTime 이후만 선택 가능).
 * 날짜가 다르거나 기준값이 없으면 그 시간만으로는 제한이 없어 빈 배열을 돌려줍니다.
 */
export function disabledTimesAtOrBefore(date: string, boundDate: string, boundTime: string): string[] {
  if (!date || !boundDate || !boundTime || date !== boundDate) return [];
  return TIME_OPTIONS.filter((option) => option <= boundTime);
}

/**
 * date 가 boundDate 와 같은 날일 때, boundTime 이상(>=)인 TIME_OPTIONS 를 막습니다 (= boundTime 이전만 선택 가능).
 */
export function disabledTimesAtOrAfter(date: string, boundDate: string, boundTime: string): string[] {
  if (!date || !boundDate || !boundTime || date !== boundDate) return [];
  return TIME_OPTIONS.filter((option) => option >= boundTime);
}

/**
 * 카카오맵이 돌려준 주소("서울 강남구 역삼동 736-1")에서 시/도 · 구/군을 뽑습니다.
 * 카카오 주소는 백엔드가 실제로 쓰는 짧은 표기("서울" · "경기" 등)와 형식이 같습니다.
 */
export function splitRegion(address: string): { region: string; district: string } {
  const [region = '', district = ''] = address.trim().split(/\s+/);
  return { region, district };
}

/** 수정 화면 확인용 예시 값 (Figma 공고 작성 입력 예시 506:2944) */
export const SAMPLE_FORM: PostFormValues = {
  title: '서울 아산 병원 동행 요청드립니다.',
  hospitalName: '서울아산병원',
  hospitalAddress: '서울 송파구 올림픽로43길 88',
  hospitalLat: 37.5266,
  hospitalLng: 127.1084,
  region: '서울',
  district: '송파구',
  departure: '서울 강동구 양재대로87길 18',
  pickupLat: 37.5301,
  pickupLng: 127.1237,
  date: '2026-09-20',
  startTime: '10:00',
  endTime: '13:30',
  hourlyPay: '14,000',
  negotiable: false,
  transportOut: '택시',
  transportBack: '택시',
  party: '1명 (본인만)',
  reportRequested: true,
  description: '다리가 불편하여 진료를 보러 갑니다.\n동행 시 넘어지지 않도록 잘 살펴주시고,\n심심하지 않게 이야기를 잘 해주는 매니저 였으면 좋겠습니다.',
  note: '병원은 11:00 예약 해두었습니다.',
  recruitStartDate: '2026-09-11',
  recruitStartTime: '13:00',
  recruitEndDate: '2026-09-19',
  recruitEndTime: '00:00',
};
