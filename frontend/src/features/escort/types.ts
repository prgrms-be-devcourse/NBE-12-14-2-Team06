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

/**
 * 보고서 작성·조회 화면 "기본 정보" 카드에 들어가는 값 (실제 API 로 채웁니다 — model/mapper.ts).
 * 의뢰인명은 여기 없습니다. 내 지원 목록(MyApplicationDto)·공고(PostDto) 어느 쪽도 의뢰인 이름을
 * 내려주지 않아서, 조회 화면에서만 보고서 응답의 ReportDto.clientName 을 씁니다.
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

/**
 * 동행 현황 화면 하나 (신청 한 건에 대응). 실제 API 로 채웁니다 — model/mapper.ts.
 * 의뢰인명·연락처·보호자 정보는 GET .../client-profile 로, 진행 단계(doneCount)는
 * GET .../progress 로 채웁니다(둘 다 application/api.ts).
 */
export type TrackingCase = {
  /** 내가 신청한 공고(신청 번호) */
  applicationId: number;
  postId: number;
  title: string;
  hospitalName: string;
  hospitalAddress: string;
  /** "2026.09.22(화)" */
  dateLabel: string;
  /** "오전 9:00" */
  timeLabel: string;
  /** "2026.09.22(화) 오전 9:00" */
  startAt: string;
  endAt: string;
  /** "갈 때 택시 · 올 때 대중교통" */
  transport: string;
  /** 환자 특이사항. 의뢰인이 안 썼으면 "없음" */
  note: string;
  clientName: string;
  clientPhone: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  /** 의뢰인 특이사항. 안 썼으면 "없음" */
  careNote: string;
  /** 완료한 진행 단계 수 (0~6) — GET .../progress 로 조회한 실제 값입니다. */
  doneCount: number;
};
