'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /mypage/applications (내가 신청한 공고) */
const MyApplicationsPage = dynamic(
  () => import('@/features/mypage').then((m) => m.MyApplicationsPage),
  {
    ssr: false,
    loading: () => <div className="min-h-screen bg-white" />,
  },
);

export default function Page() {
  return <MyApplicationsPage />;
}
