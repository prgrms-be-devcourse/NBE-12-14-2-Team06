'use client';

import type { ComponentProps, ReactNode } from 'react';
import Header from './Header';
import Footer from './Footer';

type Props = {
  children: ReactNode;
  /** 로그인한 사용자 (Header 로 전달) */
  user?: ComponentProps<typeof Header>['user'];
};

/**
 * 모든 페이지가 공유하는 클라이언트 셸.
 *
 * Header/Footer 를 app/layout.tsx 에 넣으면 그 부분만 서버에서 렌더링되므로,
 * CSR로 통일하기 위해 각 페이지의 CSR 진입 컴포넌트가 이 셸을 감쌉니다.
 */
export default function AppShell({ children, user }: Props) {
  return (
    <>
      <Header user={user} />
      <main>{children}</main>
      <Footer />
    </>
  );
}
