import type { TimelineStep } from '@/features/escort';

/** 내가 작성한 공고의 진행 상태 (백엔드 PostStatus: OPEN → MATCHED → IN_PROGRESS → COMPLETED) */
export type ClientPostStatus = 'open' | 'matched' | 'inProgress' | 'completed';

/** "내가 작성한 공고" 카드 한 장 (모의 데이터용 모양) */
export type ClientPost = {
  id: number;
  title: string;
  hospitalName: string;
  /** "서울 서초구" */
  location: string;
  /** "9월 22일(화)" */
  dateLabel: string;
  /** "오전 9:00" */
  timeLabel: string;
  durationLabel: string;
  payLabel: string;
  description: string[];
  status: ClientPostStatus;
  /** 매칭된 동행 매니저 이름 (모집 중이면 없음) */
  managerName?: string;
  /** 동행 현황 화면으로 이동할 신청 번호 (매칭 이후에만 있음) */
  applicationId?: number;
};

/**
 * 동행 매니저 프로필 (지원자 카드 · 동행 정보 카드 공용).
 * ⚠️ 지역(region)·태그(tags)는 백엔드 지원자 프로필 API(escort-profile)에 없어서, 실제 데이터로 채울 땐 빠집니다.
 */
export type Manager = {
  name: string;
  rating: number;
  completedCount: number;
  /** "서울 강남구". 값이 없으면 화면에서 이 줄을 생략합니다. */
  region?: string;
  tags?: string[];
  intro: string[];
};

/** 공고에 지원한 동행 매니저 */
export type Applicant = Manager & { id: number };

/**
 * 의뢰인이 보는 동행 진행 단계.
 * ready(매칭 완료) → ongoing(동행 중) → arrived(병원 도착, 의뢰인이 "동행 종료" 가능)
 * → finishing(귀가 완료, 종료 확정·추가 결제) → done(동행 완료)
 */
export type ClientEscortStage = 'ready' | 'ongoing' | 'arrived' | 'finishing' | 'done';

/** 제출된 진료 보고서 */
export type ClientReport = {
  department: string;
  purpose: string;
  summary: string[];
  aiSummary: string[];
  notes: string[];
  photoCount: number;
};

/** 의뢰인 동행 현황 화면 하나 (신청 한 건에 대응). 모의 데이터용 모양 */
export type ClientEscortCase = {
  applicationId: number;
  postId: number;
  stage: ClientEscortStage;
  title: string;
  hospitalName: string;
  region: string;
  /** "2026년 9월 22일(화) 오전 9:00" */
  scheduleLabel: string;
  durationLabel: string;
  payLabel: string;
  startAt: string;
  endAt: string;
  /** 종료 확정 시간 (finishing 단계에서만) */
  confirmedEndAt?: string;
  transport: string;
  meetingPlace: string;
  /** 지도 "최근 업데이트" 시각 */
  updatedAt: string;
  manager: Manager;
  timeline: TimelineStep[];
  report?: ClientReport;
};

/** 공고 작성 폼에 채워 넣는 값 (수정 화면에서 사용) */
export type PostFormValues = {
  title: string;
  hospitalName: string;
  region: string;
  district: string;
  departure: string;
  date: string;
  startTime: string;
  endTime: string;
  hourlyPay: string;
  negotiable: boolean;
  transportOut: string;
  transportBack: string;
  party: string;
  reportRequested: boolean;
  description: string;
  note: string;
  recruitStartDate: string;
  recruitStartTime: string;
  recruitEndDate: string;
  recruitEndTime: string;
};
