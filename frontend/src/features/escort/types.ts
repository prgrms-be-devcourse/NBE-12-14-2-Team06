/** 동행 진행 단계: 매칭 완료(시작 전) → 동행 중 → 동행 완료 */
export type EscortStage = 'ready' | 'ongoing' | 'done';

/** 실시간 현황 타임라인 한 줄 */
export type TimelineStep = {
  label: string;
  description: string;
  /** 완료한 시각 ("9:00") */
  time?: string;
  done: boolean;
};

/** 제출한 동행 보고서 */
export type EscortReport = {
  department: string;
  purpose: string;
  summary: string[];
  notes: string[];
  photoCount: number;
  writer: string;
  submittedAt: string;
};

/**
 * 보고서 작성 화면 "기본 정보" 카드에 들어가는 값 (실제 API 로 채웁니다 — model/mapper.ts).
 * 의뢰인명은 백엔드 응답에 없어서 빠져 있습니다.
 */
export type ReportTarget = {
  applicationId: number;
  postId: number;
  title: string;
  hospitalName: string;
  /** "2026.09.22(화)" */
  dateLabel: string;
  /** 동행 시간 "9:00 ~ 12:10" */
  workTime: string;
  hospitalAddress: string;
  /** 환자 특이사항. 의뢰인이 안 썼으면 "없음" */
  note: string;
};

/** 동행 현황 화면 하나 (신청 한 건에 대응). 모의 데이터용 모양 */
export type EscortCase = {
  /** 내가 신청한 공고(신청 번호) */
  applicationId: number;
  postId: number;
  stage: EscortStage;
  title: string;
  hospitalName: string;
  /** "2026.09.22(화)" */
  dateLabel: string;
  timeLabel: string;
  /** 동행 시간 "9:00 ~ 12:10" */
  workTime: string;
  clientName: string;
  clientPhone: string;
  guardianName: string;
  guardianPhone: string;
  hospitalAddress: string;
  note: string;
  startAt: string;
  endAt: string;
  transport: string;
  timeline: TimelineStep[];
  report?: EscortReport;
};
