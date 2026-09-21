'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /signup/terms (회원가입 3단계: 약관 동의) */
const SignupTermsPage = dynamic(() => import('@/features/signup').then((m) => m.SignupTermsPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <SignupTermsPage />;
}
