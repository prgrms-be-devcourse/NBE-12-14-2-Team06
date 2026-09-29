'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /support (고객센터) */
const SupportPage = dynamic(() => import('@/features/support').then((m) => m.SupportPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <SupportPage />;
}
