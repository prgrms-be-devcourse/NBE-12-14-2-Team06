/** 상태 라벨 색 (Figma 라벨 컴포넌트) */
export type LabelTone = 'green' | 'red' | 'blue' | 'purple' | 'gray' | 'strong';

/** 목록 카드 왼쪽 위 라벨: 신규 / 오늘 마감 / 모집 중 / 마감(지원 불가) */
export type PostBadge = 'new' | 'closing' | 'open' | 'closed';

/**
 * 공고 목록 카드용 요약. 백엔드 응답(PostDto)을 model/mapper.ts 의 toPostSummary 로 바꾼 모양입니다.
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
  /** 동행 시작일이 오늘로부터 며칠 뒤인지 (0 = 오늘). 카드에서 날짜로 다시 계산해 보여줍니다. */
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
  pay: 'all' | '10000' | '12000' | '14000';
  sort: PostSort;
  /** true = 모집중 탭(OPEN만), false = 마감 탭(OPEN 아닌 것만) */
  openOnly: boolean;
};
/** 백엔드 공고 응답 (PostDto) — 목록·상세 API 가 돌려주는 모양 그대로 */
export type PostDto = {
  id: number;
  client_id: string;            // 작성자 아이디 (username)
  title: string;
  content: string;
  patientNote: string | null;   // 선택 항목이라 비어 있을 수 있음
  region: string;
  hospitalName: string;
  hospitalAddress: string;
  hospitalLat: number;
  hospitalLng: number;
  pickupAddress: string;
  pickupLat: number;
  pickupLng: number;
  hourlyPay: number;
  recruitStartAt: string;       // "2026-09-30T09:00:00" (날짜도 JSON에서는 문자열)
  recruitEndAt: string;
  escortStartAt: string;
  escortEndAt: string;
  escortHours: number;          // 백엔드는 BigDecimal 이지만 JSON에서는 숫자
  totalPay: number;
  postStatus: string;           // "모집 중" 같은 한글 문구
  reportRequired: boolean;
  createdAt: string;
  updatedAt: string;
};