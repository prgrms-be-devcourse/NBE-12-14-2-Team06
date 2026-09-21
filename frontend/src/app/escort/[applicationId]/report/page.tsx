'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /escort/[applicationId]/report (동행 보고서 상세) */
const ReportDetailPage = dynamic(() => import('@/features/escort').then((m) => m.ReportDetailPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ReportDetailPage />;
}
