'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/escort/[applicationId]/report (진료 보고서) */
const ClientReportPage = dynamic(() => import('@/features/client').then((m) => m.ClientReportPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ClientReportPage />;
}
