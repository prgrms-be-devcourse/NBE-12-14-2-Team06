'use client';

import type { ReactNode } from 'react';
import Header from './Header';
import Footer from './Footer';

/**
 * 모든 페이지가 공유하는 클라이언트 셸.
 *
 * Header/Footer 를 app/layout.tsx 에 넣으면 그 부분만 서버에서 렌더링되므로,
 * CSR로 통일하기 위해 각 페이지의 CSR 진입 컴포넌트가 이 셸을 감쌉니다.
 */
export default function AppShell({ children }: { children: ReactNode }) {
  return (
    <>
      <Header />
      <main>{children}</main>
      <Footer />
    </>
  );
}
