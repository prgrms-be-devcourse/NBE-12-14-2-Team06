'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /mypage/education (교육 영상 목록) */
const EducationListPage = dynamic(() => import('@/features/education').then((m) => m.EducationListPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <EducationListPage />;
}
