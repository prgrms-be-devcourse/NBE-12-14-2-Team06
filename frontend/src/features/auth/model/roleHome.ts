import type { CurrentUser } from '../types';

type Role = CurrentUser['role'];

/** 로그인 직후 역할별로 처음 보여 줄 화면 */
export const LOGIN_HOME_BY_ROLE: Record<Role, string> = {
  CLIENT: '/client',
  ESCORT: '/escort/posts',
  ADMIN: '/admin',
};

/** 헤더에서 이름을 눌렀을 때 가는 마이페이지 */
export const MYPAGE_BY_ROLE: Record<Role, string> = {
  CLIENT: '/client',
  ESCORT: '/mypage',
  ADMIN: '/admin',
};
