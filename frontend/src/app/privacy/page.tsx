'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /privacy (개인정보 처리방침) */
const PrivacyPolicyPage = dynamic(() => import('@/features/legal').then((m) => m.PrivacyPolicyPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <PrivacyPolicyPage />;
}
