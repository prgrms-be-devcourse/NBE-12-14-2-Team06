import type { MyPageRole } from '../types';

export type MyPageMenuItem = { label: string; href: string };

/**
 * 역할별 마이페이지 메뉴.
 * 마이페이지 왼쪽 메뉴(MyPageShell)와 헤더의 이름 옆 드롭다운(UserMenu)이 함께 씁니다.
 *
 * TODO: 설정 화면은 아직 디자인/구현이 없습니다.
 */
export const MYPAGE_MENUS: Record<MyPageRole, MyPageMenuItem[]> = {
  escort: [
    { label: '내 정보', href: '/mypage' },
    { label: '내가 신청한 공고', href: '/mypage/applications' },
    { label: '내 정산', href: '/mypage/settlements' },
    { label: '받은 리뷰', href: '/mypage/reviews' },
    { label: '교육 영상', href: '/mypage/education' },
    { label: '설정', href: '#' },
  ],
  client: [
    { label: '내 정보', href: '/client' },
    { label: '작성한 공고', href: '/client/posts' },
    { label: '설정', href: '#' },
  ],
};
