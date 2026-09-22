'use client';

import type { ComponentProps, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
// 배럴(@/features/auth)로 가져오면 features/auth/index → LoginPage → @/components/layout → AppShell 로
// 순환 import 가 생겨서, 이 두 개만 모듈 경로로 직접 가져옵니다.
import { useAuth } from '@/features/auth/lib/AuthProvider';
import { MYPAGE_BY_ROLE } from '@/features/auth/model/roleHome';
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

  const session = me ? { name: me.name, href: MYPAGE_BY_ROLE[me.role] } : undefined;

  const handleLogout = async () => {
    await signOut();
    router.replace('/');
  };

  return (
    <>
      <Header user={user ?? session} pending={!user && loading} onLogout={handleLogout} />
      <main>{children}</main>
      <Footer />
    </>
  );
}
