'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/new/payment (결제) */
const PaymentPage = dynamic(() => import('@/features/client').then((m) => m.PaymentPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <PaymentPage />;
}
