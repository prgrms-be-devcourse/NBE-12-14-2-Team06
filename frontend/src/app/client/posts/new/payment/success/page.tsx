'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/new/payment/success (토스 successUrl) */
const SuccessPage = dynamic(() => import('@/features/client').then((m) => m.PaymentSuccessPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-[#e8f3ff]" />,
});

export default function Page() {
  // 토스 위젯 CSS 의 배경색을 이 라우트에만 적용 (globals 의 body 를 건드리지 않기 위해)
  return (
    <div className="min-h-screen bg-[#e8f3ff] py-10">
      <SuccessPage />
    </div>
  );
}
