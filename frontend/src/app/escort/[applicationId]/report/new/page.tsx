'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /escort/[applicationId]/report/new (동행 보고서 작성) */
const ReportWritePage = dynamic(() => import('@/features/escort').then((m) => m.ReportWritePage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ReportWritePage />;
}
