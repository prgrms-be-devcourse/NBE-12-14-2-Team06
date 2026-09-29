'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /escort/[applicationId] (동행 현황) */
const TrackingPage = dynamic(() => import('@/features/escort').then((m) => m.TrackingPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <TrackingPage />;
}
