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

/** 정산 상태 */
export type SettlementStatus = 'waiting' | 'done';

export type Settlement = {
  id: number;
  applicationId: number;
  /** 상세보기로 이동할 공고 번호 */
  postId: number;
  status: SettlementStatus;
  title: string;
  hospitalName: string;
  location: string;
  /** "2026.10.28 (수)" */
  dateLabel: string;
  dueLabel: string;
  /** 정산 예정일 (기간 검색용, YYYY-MM-DD) */
  dueDate: string;
  durationLabel: string;
  amount: number;
};

/** 받은 리뷰 (별점 이미지가 3~5점만 있어 3~5로 제한) */
export type Review = {
  id: number;
  title: string;
  hospitalName: string;
  location: string;
  /** 작성일 (기간 검색용, YYYY-MM-DD) */
  date: string;
  rating: 3 | 4 | 5;
  positives: string[];
  negatives: string[];
  comment: string;
};

/** 최근 활동 요약 한 칸 */
export type ActivityStat = {
  label: string;
  count: number;
  icon: string;
};

export type MyProfile = {
  name: string;
  roleLabel: string;
  username: string;
  email: string;
  phone: string;
  address: string;
  birthDate: string;
  gender: string;
  intro: string[];
};
