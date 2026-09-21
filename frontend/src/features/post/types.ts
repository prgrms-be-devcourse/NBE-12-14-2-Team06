/** 상태 라벨 색 (Figma 라벨 컴포넌트) */
export type LabelTone = 'green' | 'red' | 'blue' | 'purple' | 'gray' | 'strong';

/** 목록 카드 왼쪽 위 라벨: 신규 / 오늘 마감 / 모집 중 */
export type PostBadge = 'new' | 'closing' | 'open';

/**
 * 공고 목록 카드용 요약. (모의 데이터용 모양)
 * 백엔드 PostDto 와 대응: title, hospitalName, region, escortStartAt, escortHours, hourlyPay, content, postStatus, createdAt
 */
export type PostSummary = {
  id: number;
  title: string;
  hospitalName: string;
  /** 시/도 (필터 기준) */
  region: string;
  /** 구/군 */
  district: string;
  /** "2시간 전" */
  postedAgo: string;
  /** 동행 시작일이 오늘로부터 며칠 뒤인지 (모의 데이터가 항상 "가까운 미래"로 보이게) */
  startsInDays: number;
  /** "오전 9:00" */
  startTime: string;
  hours: number;
  hourlyPay: number;
  description: string[];
  badge: PostBadge;
};

/** 공고 상세 (요약 + 상세 화면 전용 항목) */
export type PostDetail = PostSummary & {
  /** "2026.09.12   18:12" */
  postedAt: string;
  department: string;
  transport: string;
  /** 공고 설명 (문단) */
  details: string[];
  /** 요청사항 / 특이사항 */
  requests: string[];
  clientType: string;
  withGuardian: string;
  genderPreference: string;
  clientIntro: string[];
};

export type PostSort = 'latest' | 'payHigh' | 'payLow';

/** 목록 화면 검색·필터 상태 ('all' = 조건 없음) */
export type PostFilters = {
  keyword: string;
  region: string;
  period: 'all' | 'today' | 'week' | 'month';
  pay: 'all' | '15000' | '16000';
  sort: PostSort;
};
