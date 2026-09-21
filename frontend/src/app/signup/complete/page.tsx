'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /signup/complete (회원가입 4단계: 완료) */
const SignupCompletePage = dynamic(
  () => import('@/features/signup').then((m) => m.SignupCompletePage),
  {
    ssr: false,
    loading: () => <div className="min-h-screen bg-white" />,
  },
);

export default function Page() {
  return <SignupCompletePage />;
}
