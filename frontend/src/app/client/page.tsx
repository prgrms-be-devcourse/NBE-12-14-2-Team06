'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client (의뢰인 내 정보) */
const MyInfoPage = dynamic(() => import('@/features/mypage').then((m) => m.MyInfoPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <MyInfoPage role="client" />;
}
