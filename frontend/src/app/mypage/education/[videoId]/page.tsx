'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /mypage/education/[videoId] (교육 영상 시청) */
const EducationVideoPage = dynamic(() => import('@/features/education').then((m) => m.EducationVideoPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <EducationVideoPage />;
}
