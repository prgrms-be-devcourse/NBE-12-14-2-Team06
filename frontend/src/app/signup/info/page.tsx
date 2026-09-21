'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /signup/info (회원가입 2단계: 정보 입력) */
const SignupInfoPage = dynamic(() => import('@/features/signup').then((m) => m.SignupInfoPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <SignupInfoPage />;
}
