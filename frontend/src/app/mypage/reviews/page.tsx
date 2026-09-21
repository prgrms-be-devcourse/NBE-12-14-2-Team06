'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /mypage/reviews (받은 리뷰) */
const MyReviewsPage = dynamic(() => import('@/features/mypage').then((m) => m.MyReviewsPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <MyReviewsPage />;
}
