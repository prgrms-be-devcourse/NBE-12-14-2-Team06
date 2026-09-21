'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/[postId]/applicants (지원자 확인) */
const ApplicantsPage = dynamic(() => import('@/features/client').then((m) => m.ApplicantsPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ApplicantsPage />;
}
