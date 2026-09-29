'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/escort/[applicationId] (의뢰인 동행 현황) */
const ClientTrackingPage = dynamic(() => import('@/features/client').then((m) => m.ClientTrackingPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ClientTrackingPage />;
}
