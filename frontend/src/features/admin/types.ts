/** 관리자 화면에서 다루는 회원 역할 */
export type MemberRole = 'client' | 'manager';

/** 회원 관리 목록의 한 줄 (Figma 571:19168 표의 열 구성) */
export type Member = {
  /** 화면에 그대로 보여주는 회원 ID ("user001") */
  id: string;
  name: string;
  role: MemberRole;
  email: string;
  /** "010-0000-0000" */
  phone: string;
  /** 가입일 (정렬용, YYYY-MM-DD) */
  joinedAt: string;
};

/** 최근 활동 카드 한 줄 ("최근 리뷰 · 친절해요! · 2026.02.09") */
export type RecentActivity = {
  /** 화면 왼쪽 라벨 */
  label: string;
  /** 라벨 옆 설명. 없으면 날짜만 보여줍니다. */
  detail?: string;
  /** "2026.10.01" */
  date: string;
};

/** 회원 상세 화면이 쓰는 정보 (Figma 571:20504 관리자_회원상세) */
export type MemberDetail = Member & {
  /** "활동중" 처럼 화면에 그대로 보여주는 상태 */
  status: string;
  /** "1950.01.01" */
  birthDate: string;
  /** "선택 안 함" 을 포함한 표시용 문구 */
  gender: string;
  /** "서울특별시 강남구" */
  address: string;
  /** 보호자 실명 */
  guardianName: string;
  /** 보호자 전화번호 */
  guardianPhone: string;
  /** 특이사항 */
  careNote: string;
  /** "최근 활동" 카드 (최근 로그인 · 최근 리뷰 · 최근 매칭) */
  recent: RecentActivity[];
  /** "최근 활동 요약" 카드의 네 칸 */
  activity: { label: string; count: number; icon: string }[];
};
