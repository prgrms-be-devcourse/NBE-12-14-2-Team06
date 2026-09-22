/** 내가 신청한 공고의 진행 상태 */
export type ApplicationStatus = 'pending' | 'matched' | 'inProgress' | 'completed' | 'rejected';

/** 신청 카드 한 장 (모의 데이터용 모양) */
export type Application = {
  id: number;
  /** 상세보기로 이동할 공고 번호 */
  postId: number;
  title: string;
  hospitalName: string;
  /** "서울 양천구" */
  location: string;
  /** "2026.11.16.(금)" */
  dateLabel: string;
  /** "오후 15:00" */
  timeLabel: string;
  durationLabel: string;
  payLabel: string;
  description: string[];
  status: ApplicationStatus;
};

/** 백엔드 SettlementStatus */
export type SettlementStatus = 'PENDING' | 'COMPLETED' | 'FAILED';

/** 백엔드 SettlementResponse 의 post 부분 (settlement 카드에 필요한 값만) */
export type SettlementPost = {
  id: number;
  title: string;
  hospitalName: string;
  region: string;
  escortStartAt: string;
  escortHours: number;
};

/** 백엔드 GET /api/v1/settlements 응답 한 줄 (SettlementResponse) */
export type SettlementDto = {
  id: number;
  payoutAmount: number;
  platformFee: number;
  status: SettlementStatus;
  /** 정산 완료된 날짜. 아직 정산 전이면 null */
  settledAt: string | null;
  post: SettlementPost;
};

/** 백엔드 ReviewTag(긍정/부정 태그) 이름 → 화면 문구 */
export type ReviewTagName =
  | 'KIND'
  | 'PUNCTUAL'
  | 'DETAILED_REPORT'
  | 'GOOD_COMMUNICATION'
  | 'CAREFUL'
  | 'LATE'
  | 'POOR_COMMUNICATION'
  | 'UNKIND'
  | 'INSUFFICIENT_REPORT';

/** 백엔드 GET /api/v1/users/{userId}/reviews 응답 한 줄 (ReviewDto) */
export type ReviewDto = {
  id: number;
  applicationId: number;
  rating: number;
  tags: ReviewTagName[];
  content: string | null;
  createdAt: string;
};

/** 최근 활동 요약 한 칸 */
export type ActivityStat = {
  label: string;
  count: number;
  icon: string;
};

/** 의뢰인 마이페이지(role='client')용 모의 프로필. "/client" 는 이번 작업 범위가 아니라 그대로 둡니다. */
export type MyProfile = {
  name: string;
  roleLabel: string;
  username: string;
  email: string;
  phone: string;
  address: string;
  birthDate: string;
  gender: string;
  intro?: string[];
  guardian?: { name: string; phone: string; careNote: string };
};

/** 백엔드 GET/PUT /api/v1/users/profile/escort 응답 (EscortProfileResponse) */
export type EscortProfileDto = {
  userId: number;
  name: string;
  region: string;
  intro: string | null;
  averageRating: number | null;
  completedCount: number;
  verified: boolean;
  bankName: string | null;
  accountHolder: string | null;
  accountNumber: string | null;
};

/** 마이페이지를 보는 사람. 왼쪽 메뉴와 내용이 달라집니다. */
export type MyPageRole = 'escort' | 'client';
