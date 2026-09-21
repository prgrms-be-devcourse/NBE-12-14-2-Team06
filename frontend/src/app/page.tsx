'use client';

import dynamic from 'next/dynamic';

/**
 * CSR 진입점.
 * app/ 는 "어떤 URL에 어떤 도메인을 붙일지"만 결정하고, 화면은 features/ 가 가집니다.
 */
const MainPage = dynamic(() => import('@/features/main').then((m) => m.MainPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <MainPage />;
}
