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
