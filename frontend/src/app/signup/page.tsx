'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /signup (회원가입 1단계: 가입 유형 선택) */
const SignupSelectPage = dynamic(
  () => import('@/features/signup').then((m) => m.SignupSelectPage),
  {
    ssr: false,
    loading: () => <div className="min-h-screen bg-white" />,
  },
);

export default function Page() {
  return <SignupSelectPage />;
}
