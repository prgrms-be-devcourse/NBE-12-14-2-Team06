'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /login */
const LoginPage = dynamic(() => import('@/features/auth').then((m) => m.LoginPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <LoginPage />;
}
