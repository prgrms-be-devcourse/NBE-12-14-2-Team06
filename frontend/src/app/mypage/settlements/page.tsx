'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /mypage/settlements (정산 목록) */
const MySettlementsPage = dynamic(() => import('@/features/mypage').then((m) => m.MySettlementsPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <MySettlementsPage />;
}
