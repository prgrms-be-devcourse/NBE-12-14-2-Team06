'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/[postId]/applicants/[applicationId]/profile (지원자 프로필 더보기) */
const ApplicantProfilePage = dynamic(() => import('@/features/client').then((m) => m.ApplicantProfilePage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ApplicantProfilePage />;
}
