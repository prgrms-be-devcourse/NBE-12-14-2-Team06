'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/escort/[applicationId]/review (리뷰 작성) */
const ClientReviewPage = dynamic(() => import('@/features/client').then((m) => m.ClientReviewPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ClientReviewPage />;
}
