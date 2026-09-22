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
 * 백엔드 진료 보고서 응답 (ReportDto) — 작성·조회 API 가 돌려주는 모양 그대로.
 * department 는 응답에서 한글 과목명("정형외과")으로 내려옵니다. 요청 시에는 ENUM 이름을 보내야 합니다 (model/departments.ts).
 */
export type ReportDto = {
  id: number;
  applicationId: number;
  title: string;
  department: string;
  purpose: string;
  originContent: string;
  notes: string | null;
  aiSummary: string | null;
  summarizedAt: string | null;
  createdAt: string;
  updatedAt: string;
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
