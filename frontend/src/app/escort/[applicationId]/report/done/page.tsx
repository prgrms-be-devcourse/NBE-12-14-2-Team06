'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /escort/[applicationId]/report/done (보고서 제출 완료) */
const ReportDonePage = dynamic(() => import('@/features/escort').then((m) => m.ReportDonePage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ReportDonePage />;
}
