import type { PostFormValues } from '../types';

/*
 * 공고 작성 폼의 선택지와 계산 도우미입니다.
 * ⚠️ 구/군 목록은 화면 확인용으로 일부 지역만 적었습니다. 지역 코드 API 가 정해지면 교체하세요.
 */
export const DISTRICTS: Record<string, string[]> = {
  서울특별시: [
    '강남구', '강동구', '강북구', '강서구', '관악구', '광진구', '구로구', '금천구', '노원구', '도봉구', '동대문구', '동작구', '마포구',
    '서대문구', '서초구', '성동구', '성북구', '송파구', '양천구', '영등포구', '용산구', '은평구', '종로구', '중구', '중랑구',
  ],
  경기도: ['성남시', '수원시', '고양시', '용인시', '부천시', '안산시', '안양시', '남양주시', '화성시', '평택시'],
  부산광역시: ['강서구', '금정구', '남구', '동구', '동래구', '부산진구', '북구', '사상구', '사하구', '서구', '수영구', '연제구', '영도구', '중구', '해운대구'],
  인천광역시: ['계양구', '남동구', '동구', '미추홀구', '부평구', '서구', '연수구', '중구'],
};
export const DEFAULT_DISTRICTS = ['전체'];

/** 00:00 ~ 23:30, 30분 간격 */
export const TIME_OPTIONS: string[] = Array.from({ length: 48 }, (_, index) => {
  const hour = String(Math.floor(index / 2)).padStart(2, '0');
  return `${hour}:${index % 2 ? '30' : '00'}`;
});

export const TRANSPORT_OPTIONS = ['도보', '택시', '자가용', '대중교통', '휠체어 택시(콜택시)', '기타'];
export const PARTY_OPTIONS = ['1명 (본인만)', '2명 (보호자 동반)', '3명 이상'];

export const EMPTY_FORM: PostFormValues = {
  title: '', hospitalName: '', region: '', district: '', departure: '', date: '', startTime: '', endTime: '', hourlyPay: '',
  negotiable: false, transportOut: '', transportBack: '', party: '', reportRequested: false, description: '', note: '',
  recruitStartDate: '', recruitStartTime: '', recruitEndDate: '', recruitEndTime: '',
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

/** 수정 화면 확인용 예시 값 (Figma 공고 작성 입력 예시 506:2944) */
export const SAMPLE_FORM: PostFormValues = {
  title: '서울 아산 병원 동행 요청드립니다.',
  hospitalName: '서울 아산 병원',
  region: '서울특별시',
  district: '송파구',
  departure: '서울특별시 강동구 양재대로87번길 18',
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
