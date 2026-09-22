/** 백엔드 ApplicationStatus (지원 진행 상태) */
export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELED' | 'NO_SHOW';

/** 백엔드 EscortProgress (동행 진행 단계). 이 순서대로만 진행할 수 있습니다. */
export type EscortProgress = 'NOT_STARTED' | 'DEPARTED' | 'TO_HOSPITAL' | 'AT_HOSPITAL' | 'TO_HOME' | 'ARRIVED_HOME';

export const PROGRESS_ORDER: EscortProgress[] = [
  'NOT_STARTED',
  'DEPARTED',
  'TO_HOSPITAL',
  'AT_HOSPITAL',
  'TO_HOME',
  'ARRIVED_HOME',
];

/** POST /api/v1/applications/{postId} 응답 */
export type ApplicationApplyDto = {
  id: number;
  postId: number;
  status: ApplicationStatus;
};

/** GET /api/v1/applications/posts/{postId} 목록의 한 줄 */
export type ApplicantDto = {
  applicationId: number;
  escortId: number;
  escortName: string;
  status: ApplicationStatus;
};

/** PATCH .../accept 응답 */
export type ApplicationAcceptDto = {
  applicationId: number;
  postId: number;
  escortId: number;
  status: ApplicationStatus;
};

/** GET /api/v1/applications/{applicationId}/escort-profile 응답 */
export type EscortProfileDto = {
  escortId: number;
  name: string;
  verified: boolean;
  /** 자기소개를 아직 안 썼으면 null */
  intro: string | null;
  completedCount: number;
  rating: number;
  ratingCount: number;
  noShowCount: number;
};

/** 화면에서 쓰기 좋게 다듬은 지원자 한 명 (목록 + 프로필을 합친 모양) */
export type Applicant = {
  applicationId: number;
  escortId: number;
  name: string;
  status: ApplicationStatus;
  /** 프로필은 별도 호출(escort-profile)로 채워집니다. 아직 안 왔으면 undefined */
  profile?: EscortProfileDto;
};
