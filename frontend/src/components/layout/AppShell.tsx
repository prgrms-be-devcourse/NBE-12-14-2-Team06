'use client';

import type { ComponentProps, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
// 배럴(@/features/auth)로 가져오면 features/auth/index → LoginPage → @/components/layout → AppShell 로
// 순환 import 가 생겨서, 이 두 개만 모듈 경로로 직접 가져옵니다.
import { useAuth } from '@/features/auth/lib/AuthProvider';
import { MYPAGE_BY_ROLE } from '@/features/auth/model/roleHome';
// 마이페이지 왼쪽 메뉴와 같은 목록을 헤더 드롭다운에도 씁니다. (배럴은 위와 같은 이유로 피합니다)
import { MYPAGE_MENUS } from '@/features/mypage/model/menu';
import Header from './Header';
import Footer from './Footer';

type Props = {
  children: ReactNode;
  /**
   * 헤더에 보여 줄 사용자.
   * 넘기지 않으면 로그인 세션에서 가져옵니다. (모의 데이터로 만든 화면만 직접 넘깁니다)
   */
  user?: ComponentProps<typeof Header>['user'];
};

/**
 * 모든 페이지가 공유하는 클라이언트 셸.
 *
 * Header/Footer 를 app/layout.tsx 에 넣으면 그 부분만 서버에서 렌더링되므로,
 * CSR로 통일하기 위해 각 페이지의 CSR 진입 컴포넌트가 이 셸을 감쌉니다.
 */
export default function AppShell({ children, user }: Props) {
  const router = useRouter();
  const { user: me, loading, signOut } = useAuth();

  // 관리자는 마이페이지 메뉴가 따로 없어서 드롭다운 없이 이름만 보여 줍니다.
  const menuRole = me?.role === 'CLIENT' ? 'client' : me?.role === 'ESCORT' ? 'escort' : undefined;

  const session = me
    ? { name: me.name, href: MYPAGE_BY_ROLE[me.role], menu: menuRole && MYPAGE_MENUS[menuRole] }
    : undefined;

  // 의뢰인·관리자만 헤더에 "공고등록"을 보여 줍니다(공고 작성 화면 자체도 이 두 역할만 들어갈 수 있음).
  const canRegisterPost = me?.role === 'CLIENT' || me?.role === 'ADMIN';
  // "공고 찾기"를 동행 매니저 전용 목록(/escort/posts)으로 보낼지 — 그 화면이 ESCORT 전용 가드가 걸려 있어서 구분합니다.
  const isEscort = me?.role === 'ESCORT';

  const handleLogout = async () => {
    await signOut();
    router.replace('/');
  };

  return (
    <>
      <Header
        user={user ?? session}
        pending={!user && loading}
        onLogout={handleLogout}
        canRegisterPost={canRegisterPost}
        isEscort={isEscort}
      />
      <main>{children}</main>
      <Footer />
    </>
  );
}
